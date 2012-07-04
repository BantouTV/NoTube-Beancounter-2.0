package tv.notube.profiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.notube.commons.linking.LinkingEngine;
import tv.notube.commons.model.Interest;
import tv.notube.commons.model.UserProfile;
import tv.notube.commons.model.activity.*;
import tv.notube.commons.model.activity.Object;
import tv.notube.commons.nlp.NLPEngine;
import tv.notube.profiler.rules.ObjectProfilingRule;
import tv.notube.profiler.rules.ProfilingRule;
import tv.notube.profiler.rules.ProfilingRuleException;
import tv.notube.profiler.utils.Utils;
import tv.notube.profiles.Profiles;
import tv.notube.profiles.ProfilesException;

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

    private static Logger LOGGER = LoggerFactory.getLogger(DefaultProfilerImpl.class);

    private Map<Class<? extends tv.notube.commons.model.activity.Object>, Class<? extends ObjectProfilingRule>>
            objectRules = new HashMap<Class<? extends Object>, Class<? extends ObjectProfilingRule>>();

    private Profiles ps;

    private NLPEngine nlpEng;

    private LinkingEngine linkEng;

    private Properties properties;

    public DefaultProfilerImpl(Profiles ps, NLPEngine nlpEng, LinkingEngine linkEng, Properties properties) {
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

    private ObjectProfilingRule getRule(Activity activity) throws ProfilerException {
        Class<? extends Object> type = activity.getObject().getClass();
        Class<? extends ObjectProfilingRule> ruleClass = objectRules.get(type);
        ObjectProfilingRule opr = build(
                ruleClass,
                type,
                activity.getObject(),
                this.getNLPEngine(),
                this.linkEng
        );
        return opr;
    }

    public UserProfile profile(UUID userId, Activity activity) throws ProfilerException {
        LOGGER.info("profiling started for user [" + userId + "]");
        // grab the old profile
        UserProfile old;
        try {
            old = getProfile(userId);
        } catch (ProfilesException e) {
            throw new ProfilerException("Error while looking up the old profile for user with id [" + userId + "]", e);
        }
        ObjectProfilingRule opr = getRule(activity);
        try {
            opr.run(properties);
        } catch (ProfilingRuleException e) {
            final String errMsg = "Error while running rule for activity [" + activity + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilerException(errMsg, e);
        }
        double multiplier = getMultiplier(activity.getVerb());
        Collection<URI> activityReferences;
        try {
            activityReferences = opr.getResult();
        } catch (ProfilingRuleException e) {
            throw new ProfilerException("Error while getting rule result", e);
        }
        if (activityReferences.size() == 0) {
            if (old != null) {
                return old;
            } else {
                UserProfile up = toProfile(userId, new HashSet<Interest>());
                up.setVisibility(UserProfile.Visibility.PUBLIC);
                try {
                    ps.store(up);
                } catch (ProfilesException e) {
                    throw new ProfilerException(
                            "Error while storing profile for user [" + userId + "]", e
                    );
                }
                return up;
            }
        }
        Collection<Interest> interests = toInterests(
                activity,
                activityReferences,
                multiplier
        );
        if (old == null) {
            // return this as a profile, it's the first one after all
            interests = limit(new ArrayList<Interest>(interests));
            interests = normalize(interests);
            UserProfile up = toProfile(userId, interests);
            try {
                ps.store(up);
            } catch (ProfilesException e) {
                throw new ProfilerException(
                        "Error while storing profile for user [" + userId + "]", e
                );
            }
            return up;
        }
        interests = merge(interests, old.getInterests());
        // return the new profile
        interests = limit(new ArrayList<Interest>(interests));
        interests = normalize(interests);
        UserProfile up = toProfile(userId, interests);
        try {
            ps.store(up);
        } catch (ProfilesException e) {
            throw new ProfilerException(
                    "Error while storing profile for user [" + userId + "]", e
            );
        }
        LOGGER.info("profiling ended for user [" + userId + "]");
        return up;
    }

    private Collection<Interest> merge(Collection<Interest> activityInterests, Set<Interest> oldInterests) {
        Collection<Interest> merged = new HashSet<Interest>();
        Collection<Interest> newInterests = new HashSet<Interest>();
        // put in merged only the nu ones
        for(Interest interest : activityInterests) {
            if(Utils.contains(interest, oldInterests)) {
                URI resource = interest.getResource();
                Interest oldInterest = Utils.retrieve(resource, oldInterests);
                int threshold = Integer.parseInt(properties.getProperty("interest.activities.limit"), 10);
                merged.add(
                        Utils.merge(interest, oldInterest, threshold)
                );
                // remove it from the old ones
                oldInterests.remove(oldInterest);
            } else {
                newInterests.add(interest);
            }
        }
        // now perform the union between newOnes and oldOnes
        List<Interest> union = Utils.union(newInterests, oldInterests);
        // if |union| + |merged| > limit, than free some space.
        if(union.size() + merged.size() > getLimit()) {
            Collections.sort(union);
            union = Utils.cut(union, union.size() - merged.size());
        }
        // then add the merged ones
        for(Interest m : merged) {
            union.add(m);
        }
        return union;
    }

    private int getLimit() {
        return Integer.parseInt(properties.getProperty("interest.limit"));
    }

    private Collection<Interest> normalize(Collection<Interest> interests) {
        double sum = 0.0d;
        for(Interest i : interests) {
            sum += i.getWeight();
        }
        for(Interest i : interests) {
            i.setWeight( i.getWeight() / sum );
        }
        return interests;
    }

    private Collection<Interest> limit(List<Interest> interests) {
        int limit = getLimit();
        if (interests.size() > limit) {
            // sort them again
            Collections.sort(interests);
            Collection<Interest> limited = new HashSet<Interest>();
            for (int i = 0; i < limit; i++) {
                limited.add(interests.get(i));
            }
            return limited;
        }
        return interests;
    }

    private UserProfile toProfile(UUID userId, Collection<Interest> interests) {
        UserProfile up = new UserProfile(userId);
        up.setInterests(new HashSet<Interest>(interests));
        up.setVisibility(UserProfile.Visibility.PUBLIC);
        return up;
    }

    private Collection<Interest> toInterests(Activity activity, Collection<URI> activityReferences, double multiplier) {
        Collection<Interest> interests = new HashSet<Interest>();
        int numOfRefs = activityReferences.size();
        for(URI reference : activityReferences) {
            Interest i = new Interest(reference);
            i.addActivity(activity.getId());
            i.setVisible(true);
            i.setWeight((7.5/multiplier) / numOfRefs);
            interests.add(i);
        }
        return interests;
    }

    private double getMultiplier(Verb verb) {
        return Double.parseDouble(properties.getProperty("verb.multiplier." + verb));
    }

    private UserProfile getProfile(UUID userId) throws ProfilesException {
        return ps.lookup(userId);
    }

    public Profiles getProfileStore() {
        return ps;
    }

    public NLPEngine getNLPEngine() {
        return nlpEng;
    }

    public LinkingEngine getLinkingEngine() {
        return linkEng;
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
            throw new ProfilerException("", e);
        }
        try {
            return constructor.newInstance(object, nlpEngine, linkEng);
        } catch (InstantiationException e) {
            throw new ProfilerException("", e);
        } catch (IllegalAccessException e) {
            throw new ProfilerException("", e);
        } catch (InvocationTargetException e) {
            throw new ProfilerException("", e);
        }
    }

}
