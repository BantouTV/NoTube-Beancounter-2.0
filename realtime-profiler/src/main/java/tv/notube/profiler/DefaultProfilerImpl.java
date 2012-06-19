package tv.notube.profiler;

import tv.notube.commons.linking.LinkingEngine;
import tv.notube.commons.model.Interest;
import tv.notube.commons.model.UserProfile;
import tv.notube.commons.model.activity.*;
import tv.notube.commons.model.activity.Object;
import tv.notube.commons.nlp.NLPEngine;
import tv.notube.profiler.rules.ObjectProfilingRule;
import tv.notube.profiler.rules.ProfilingRule;
import tv.notube.profiler.rules.ProfilingRuleException;
import tv.notube.profiler.store.ProfileStore;
import tv.notube.profiler.store.ProfileStoreException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.*;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class DefaultProfilerImpl implements Profiler {

    private Map<Class<? extends tv.notube.commons.model.activity.Object>,Class<? extends ObjectProfilingRule>>
            objectRules = new HashMap<Class<? extends Object>, Class<? extends ObjectProfilingRule>>();

    private ProfileStore ps;

    private NLPEngine nlpEng;

    private LinkingEngine linkEng;

    private Properties properties;

    public DefaultProfilerImpl(ProfileStore ps, NLPEngine nlpEng, LinkingEngine linkEng, Properties properties) {
        this.ps = ps;
        this.nlpEng = nlpEng;
        this.linkEng = linkEng;
        this.properties = properties;
    }

    public void registerRule(Class<? extends tv.notube.commons.model.activity.Object> type, Class<? extends ObjectProfilingRule> rule)
            throws ProfilerException {
        objectRules.put(type, rule);
    }

    public void registerRule(Verb verb, ProfilingRule rule) throws ProfilerException {
        throw new UnsupportedOperationException("NIY");
    }

    public UserProfile profile(UUID userId, Activity activity) throws ProfilerException {
        // grab the old profile
        UserProfile old;
        try {
            old = getProfile(userId);
        } catch (ProfileStoreException e) {
            throw new ProfilerException();
        }
        Class<? extends Object> type = activity.getObject().getClass();
        Class<? extends ObjectProfilingRule> ruleClass = objectRules.get(type);
        ObjectProfilingRule opr = build(ruleClass, type, activity.getObject(), this.getNLPEngine(), this.linkEng);
        try {
            opr.run(properties);
        } catch (ProfilingRuleException e) {
            throw new ProfilerException();
        }
        UserProfile newProfile;
        Collection<URI> activityInterests;
        try {
            activityInterests = opr.getResult();
        } catch (ProfilingRuleException e) {
            throw new ProfilerException();
        }
        if (activityInterests.size() != 0) {
            newProfile = computeNewProfile(activity, activityInterests, old, userId);
            try {
                ps.store(newProfile);
            } catch (ProfileStoreException e) {
                throw new ProfilerException();
            }
            return newProfile;
        }
        if(old != null) {
            return old;
        }
        UserProfile empty = new UserProfile(userId);
        empty.setInterests(new HashSet<Interest>());
        return new UserProfile(userId);
    }

    private UserProfile computeNewProfile(Activity activity, Collection<URI> references, UserProfile old, UUID userId) {
        Set<Interest> oldInterests = new HashSet<Interest>();
        if(old != null) {
            // grab the old interests
            oldInterests = old.getInterests();
        }
        // make unweighted interests the new ones
        Set<Interest> newInterests = new HashSet<Interest>();
        Set<URI> oldOrModifiedInterests = new HashSet<URI>();
        for(URI reference : references) {
            if(isOld(oldInterests, reference)) {
                Interest oldInterest = getInterest(oldInterests, reference);
                // reset the weight
                oldInterest.setWeight(0.0d);
                oldInterest.addActivity(activity);
                newInterests.add(oldInterest);
                oldOrModifiedInterests.add(reference);
            } else {
                Interest i = new Interest();
                i.setReference(reference);
                i.addActivity(activity);
                newInterests.add(i);
            }
        }
        // do the same for old interests
        for(Interest oldInt : oldInterests) {
            if(isOld(references, oldInt.getReference())) {
                Interest oldInterest = getInterest(oldInterests, oldInt.getReference());
                // reset the weight
                oldInterest.setWeight(0.0d);
                oldInterest.addActivity(activity);
                newInterests.add(oldInterest);
                oldOrModifiedInterests.add(oldInt.getReference());
            } else {
                Interest i = new Interest();
                i.setReference(oldInt.getReference());
                i.addActivity(activity);
                newInterests.add(i);
            }
        }
        Set<ModifiedInterest> result;
        // weight them
        result = weight(newInterests, oldOrModifiedInterests);
        // limit them, only the top 10 for example if they are more but exclude
        // the new ones or the interests that have been increased
        Set<ModifiedInterest> interests = limit(
                Integer.parseInt(properties.getProperty("interest.limit")),
                result
        );
        // normalize them: their sum must be 1
        return buildProfile(normalize(interests), userId);
    }

    private boolean isOld(Collection<URI> references, URI reference) {
        return references.contains(reference);
    }

    private Set<Interest> normalize(Set<ModifiedInterest> mis) {
        double sum = 0.0;
        for(ModifiedInterest mi : mis) {
            sum += mi.getRawWeight();
        }
        Set<Interest> interests = new HashSet<Interest>();
        for(ModifiedInterest mi : mis) {
            Interest i = mi.getInterest();
            i.setWeight(mi.getRawWeight() / sum);
            interests.add(i);
        }
        return interests;
    }

    private Set<ModifiedInterest> limit(int limit, Set<ModifiedInterest> result) {
        List<ModifiedInterest> mis = new ArrayList<ModifiedInterest>(result);
        Collections.sort(mis);
        Set<ModifiedInterest> limitedMis = new HashSet<ModifiedInterest>();
        if(result.size() <= limit) {
            return result;
        }
        // put first the modified or new ones
        // todo check if it's ordered in the correct way
        for (int i = 0; i < result.size(); i++) {
            if (mis.get(i).isModified()) {
                limitedMis.add(mis.get(i));
            }
        }
        for (int i = 0; i < limit - limitedMis.size(); i++) {
            if (!mis.get(i).isModified()) {
                limitedMis.add(mis.get(i));
            }
        }
        return limitedMis;
    }

    private Set<ModifiedInterest> weight(Set<Interest> newInterests, Set<URI> oldOrModifiedInterests) {
        Set<ModifiedInterest> modified = new HashSet<ModifiedInterest>();
        for(Interest i : newInterests) {
            double rawWeight = 0.0d;
            Collection<Activity> iActivities = i.getActivities();
            for(Activity ai : iActivities) {
                rawWeight += getMultiplier(ai.getVerb());
            }
            modified.add(new ModifiedInterest(
                    i,
                    rawWeight,
                    oldOrModifiedInterests.contains(i.getReference())
                    )
            );
        }
        return modified;
    }

    private double getMultiplier(Verb verb) {
        return Double.parseDouble(properties.getProperty("verb.multiplier." + verb));
    }

    private UserProfile buildProfile(Set<Interest> newInterests, UUID userId) {
        UserProfile up = new UserProfile(userId);
        up.setInterests(newInterests);
        up.setVisibility(UserProfile.Visibility.PUBLIC);
        return up;
    }

    private Interest getInterest(Set<Interest> oldInterests, URI reference) {
        for(Interest i : oldInterests) {
            if(i.getReference().equals(reference)) {
                return i;
            }
        }
        throw new IllegalStateException("[" + reference + "] should be present in the old interests but it's not");
    }

    private boolean isOld(Set<Interest> oldInterests, URI reference) {
        Interest i = new Interest();
        i.setReference(reference);
        for(Interest oldi : oldInterests) {
            if(oldi.getReference().equals(reference)) {
                return true;
            }
        }
        return false;
    }

    private ObjectProfilingRule build(
            Class<? extends ObjectProfilingRule> ruleClass,
            Class<? extends Object> type,
            Object object,
            NLPEngine nlpEngine,
            LinkingEngine linkEng
    ) throws ProfilerException {
        Constructor<? extends ObjectProfilingRule> constructor;
        try {
            constructor = ruleClass.getConstructor(
                    type,
                    NLPEngine.class,
                    LinkingEngine.class
            );
        } catch (NoSuchMethodException e) {
            throw new ProfilerException();
        }
        try {
            return constructor.newInstance(object, nlpEngine, linkEng);
        } catch (InstantiationException e) {
            throw new ProfilerException();
        } catch (IllegalAccessException e) {
            throw new ProfilerException();
        } catch (InvocationTargetException e) {
            throw new ProfilerException();
        }
    }

    private UserProfile getProfile(UUID userId) throws ProfileStoreException {
        return ps.lookup(userId);
    }

    public ProfileStore getProfileStore() {
        return ps;
    }

    public NLPEngine getNLPEngine() {
        return nlpEng;
    }

    public LinkingEngine getLinkingEngine() {
        return linkEng;
    }

    private class ModifiedInterest implements Comparable<ModifiedInterest> {

        private Interest i;
        private double rawWeight;
        private boolean modified;

        public ModifiedInterest(Interest i, double rawWeight, boolean modified) {
            this.i = i;
            this.rawWeight = rawWeight;
            this.modified = modified;
        }

        public Interest getInterest() {
            return i;
        }

        public double getRawWeight() {
            return rawWeight;
        }

        public boolean isModified() {
            return modified;
        }

        public int compareTo(ModifiedInterest modifiedInterest) {
            if(this.rawWeight > modifiedInterest.getRawWeight()) {
                return 1;
            } else if(this.rawWeight < modifiedInterest.getRawWeight()) {
                return -1;
            }
            return 0;
        }
    }
}
