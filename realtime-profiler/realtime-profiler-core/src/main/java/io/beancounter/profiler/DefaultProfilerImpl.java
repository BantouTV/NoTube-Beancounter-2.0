package io.beancounter.profiler;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.beancounter.commons.linking.LinkingEngine;
import io.beancounter.commons.model.Interest;
import io.beancounter.commons.model.UserProfile;
import io.beancounter.commons.model.activity.*;
import io.beancounter.commons.model.activity.Object;
import io.beancounter.commons.model.activity.facebook.Like;
import io.beancounter.commons.nlp.NLPEngine;
import io.beancounter.profiler.rules.ObjectProfilingRule;
import io.beancounter.profiler.rules.ProfilingRule;
import io.beancounter.profiler.rules.ProfilingRuleException;
import io.beancounter.profiler.rules.custom.DevNullProfilingRule;
import io.beancounter.profiler.rules.custom.FacebookLikeProfilingRule;
import io.beancounter.profiler.rules.custom.GenericObjectProfilingRule;
import io.beancounter.profiler.rules.custom.TweetProfilingRule;
import io.beancounter.profiler.utils.Utils;
import io.beancounter.profiles.Profiles;
import io.beancounter.profiles.ProfilesException;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.*;

/**
 * In-memory, default implementation of {@link Profiler}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public final class DefaultProfilerImpl implements Profiler {

    private static Logger LOGGER = LoggerFactory.getLogger(DefaultProfilerImpl.class);

    private Map<Class<? extends io.beancounter.commons.model.activity.Object>, Class<? extends ObjectProfilingRule>>
            objectRules = new HashMap<Class<? extends Object>, Class<? extends ObjectProfilingRule>>();

    private Profiles ps;

    private NLPEngine nlpEng;

    private LinkingEngine linkEng;

    private Properties properties;

    @Inject
    public DefaultProfilerImpl(
            Profiles ps,
            NLPEngine nlpEng,
            LinkingEngine linkEng,
            @Named("profilerProperties") Properties properties
    ) throws ProfilerException {
        this.ps = ps;
        this.nlpEng = nlpEng;
        this.linkEng = linkEng;
        this.properties = properties;
        // register custom rules
        // TODO (make it configurable)
        registerRule(Tweet.class, TweetProfilingRule.class);
        registerRule(io.beancounter.commons.model.activity.Object.class, GenericObjectProfilingRule.class);
        registerRule(Like.class, FacebookLikeProfilingRule.class);
    }

    public void registerRule(Class<? extends io.beancounter.commons.model.activity.Object> type, Class<? extends ObjectProfilingRule> rule)
            throws ProfilerException {
        objectRules.put(type, rule);
    }

    public void registerRule(Verb verb, ProfilingRule rule) throws ProfilerException {
        throw new UnsupportedOperationException("NIY");
    }

    private ObjectProfilingRule getRule(Activity activity) throws ProfilerException {
        Class<? extends Object> type = activity.getObject().getClass();
        Class<? extends ObjectProfilingRule> ruleClass = objectRules.get(type);
        if(ruleClass == null) {
            // if I can't handle it, then send to >> /dev/null
            ruleClass = DevNullProfilingRule.class;
        }
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
            final String errMsg = "Error while looking up the old profile for user with id [" + userId + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilerException(errMsg, e);
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
        Collection<Interest> newInterests;
        try {
            newInterests = opr.getResult();
        } catch (ProfilingRuleException e) {
            final String errMsg = "Error while getting rule result";
            LOGGER.error(errMsg, e);
            throw new ProfilerException(errMsg, e);
        }
        if (newInterests.size() == 0) {
            if (old != null) {
                return old;
            } else {
                UserProfile up = toProfile(userId, new HashSet<Interest>());
                up.setVisibility(UserProfile.Visibility.PUBLIC);
                try {
                    ps.store(up);
                } catch (ProfilesException e) {
                    final String errMsg = "Error while storing profile for user [" + userId + "]";
                    LOGGER.error(errMsg, e);
                    throw new ProfilerException(errMsg, e);
                }
                return up;
            }
        }
        Collection<Interest> interests = setIdAndWeight(
                activity.getId(),
                newInterests,
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
                final String errMsg = "Error while storing profile for user [" + userId + "]";
                LOGGER.error(errMsg, e);
                throw new ProfilerException(errMsg, e);
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
            final String errMsg = "Error while storing profile for user [" + userId + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilerException(errMsg, e);
        }
        LOGGER.info("profiling ended nicely for user [" + userId + "]");
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

    private Collection<Interest> setIdAndWeight(UUID activityId, Collection<Interest> newInterests, double multiplier) {
        normalize(newInterests);
        for (Interest i : newInterests) {
            i.addActivity(activityId);
            i.setVisible(true);
            i.setWeight((i.getWeight() * multiplier));
        }
        return newInterests;
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
        if(ruleClass.equals(DevNullProfilingRule.class)) {
            return new DevNullProfilingRule();
        }
        Constructor<? extends ObjectProfilingRule> constructor;
        try {
            constructor = ruleClass.getConstructor(
                    type,
                    NLPEngine.class,
                    LinkingEngine.class
            );
        } catch (NoSuchMethodException e) {
            final String errMsg = "cannot find a constructor with type [" + type.getName() + "]";
            throw new ProfilerException(errMsg, e);
        }
        try {
            return constructor.newInstance(object, nlpEngine, linkEng);
        } catch (Exception e) {
            final String errMsg = "error while instantiating a [" + ruleClass.getName() + "] with type [" + type.getName() + "]";
            throw new ProfilerException(errMsg, e);
        }
    }

}
