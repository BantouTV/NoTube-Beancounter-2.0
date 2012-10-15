package io.beancounter.profiler;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.beancounter.commons.model.Category;
import io.beancounter.commons.model.Topic;
import org.joda.time.DateTime;
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
            LOGGER.warn("it seems I can't handle this activity [" + activity + "], setting to /dev/null rule");
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
        UserProfile old = grabOldProfile(userId);
        // get the correct rule
        ObjectProfilingRule opr = getRule(activity);
        // run the rule
        runRule(opr, properties, activity);
        // get multiplier
        double multiplier = getMultiplier(activity.getVerb());
        UserProfile up;
        if(old != null) {
            up = update(old, opr, activity, multiplier);
            store(up);
            LOGGER.info("profiling ended nicely for user [" + userId + "]");
            return up;

        }
        up = create(opr, userId, activity, multiplier);
        store(up);
        LOGGER.info("profiling ended nicely for user [" + userId + "]");
        return up;
    }

    private void store(UserProfile up) throws ProfilerException {
        try {
            ps.store(up);
        } catch (ProfilesException e) {
            final String errMsg = "Error while storing profile for user [" + up.getUserId() + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilerException(errMsg, e);
        }
    }

    private UserProfile create(ObjectProfilingRule opr, UUID userId, Activity activity, double multiplier) throws ProfilerException {
        List<Interest> newInterests;
        try {
            newInterests = opr.getInterests();
        } catch (ProfilingRuleException e) {
            final String errMsg = "error while getting result interests for user [" + userId + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilerException(errMsg, e);
        }
        List<Interest> interests = setIdAndWeight(
                activity.getId(),
                newInterests,
                multiplier
        );
        interests = limit(interests);
        interests = normalize(interests);
        Collection<Category> newCategories;
        try {
            newCategories = opr.getCategories();
        } catch (ProfilingRuleException e) {
            final String errMsg = "error while getting result categories for user [" + userId + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilerException(errMsg, e);
        }
        List<Category> categories = setWeight(
                newCategories,
                multiplier
        );
        categories = limit(categories);
        categories = normalize(categories);
        return toProfile(userId, interests, categories);
    }

    private List<Category> setWeight(Collection<Category> newCategories, double multiplier) {
        normalize(newCategories);
        for (Category c : newCategories) {
            c.setWeight((c.getWeight() * multiplier));
        }
        return new ArrayList<Category>(newCategories);
    }

    private <T extends Topic> List<T> normalize(Collection<T> topics) {
        double sum = 0.0d;
        for(Topic t : topics) {
            sum += t.getWeight();
        }
        for(Topic t : topics) {
            t.setWeight( t.getWeight() / sum );
        }
        return new ArrayList<T>(topics);
    }

    private <T extends Topic> List<T> limit(List<T> topics) {
        int limit = getLimit();
        if (topics.size() > limit) {
            // sort them again
            Collections.sort(topics);
            Set<T> limited = new HashSet<T>();
            for (int i = 0; i < limit; i++) {
                limited.add(topics.get(i));
            }
            return new ArrayList<T>(limited);
        }
        return topics;
    }

    private UserProfile update(UserProfile old, ObjectProfilingRule opr, Activity activity, double multiplier) throws ProfilerException {
        Collection<Interest> oldInterests = old.getInterests();
        Collection<Category> oldCategories = old.getCategories();
        List<Interest> newInterests;
        try {
            newInterests = opr.getInterests();
        } catch (ProfilingRuleException e) {
            final String errMsg = "error getting interests from rule";
            throw new ProfilerException(errMsg, e);
        }
        newInterests = setIdAndWeight(
                activity.getId(),
                newInterests,
                multiplier
        );
        Collection<Interest> updatedInterests;
        updatedInterests = update(oldInterests, newInterests);

        List<Category> newCategories;
        try {
            newCategories = opr.getCategories();
        } catch (ProfilingRuleException e) {
            final String errMsg = "error getting categories from rule";
            throw new ProfilerException(errMsg, e);
        }
        newCategories = setWeight(
                newCategories,
                multiplier
        );
        Collection<Category> updatedCategories;
        updatedCategories = update(oldCategories, newCategories);
        old.setInterests(new HashSet<Interest>(updatedInterests));
        old.setCategories(new HashSet<Category>(updatedCategories));
        old.setLastUpdated(DateTime.now());
        return old;
    }

    private <T extends Topic> Collection<T> update(Collection<T> oldT, List<T> ts) {
        Collection<T> updated;
        updated = merge(ts, new HashSet<T>(oldT));
        // return the new profile
        updated = limit(new ArrayList<T>(updated));
        updated = normalize(updated);
        return updated;
    }

    private void runRule(ObjectProfilingRule opr, Properties properties, Activity activity)
            throws ProfilerException {
        try {
            opr.run(properties);
        } catch (ProfilingRuleException e) {
            final String errMsg = "Error while running rule for activity [" + activity + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilerException(errMsg, e);
        }
    }

    private UserProfile grabOldProfile(UUID userId) throws ProfilerException {
        UserProfile old;
        try {
            old = getProfile(userId);
        } catch (ProfilesException e) {
            final String errMsg = "Error while looking up the old profile for user with id [" + userId + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilerException(errMsg, e);
        }
        return old;
    }

    private <T extends Topic> Collection<T> merge(Collection<T> topicsFromActivity, Set<T> oldTopics) {
        Collection<T> merged = new HashSet<T>();
        Collection<T> newInterests = new HashSet<T>();
        // put in merged only the nu ones
        for(T t : topicsFromActivity) {
            if(Utils.contains(t, oldTopics)) {
                URI resource = t.getResource();
                T oldT = Utils.retrieve(resource, oldTopics);
                int threshold = Integer.parseInt(properties.getProperty("interest.activities.limit"), 10);
                merged.add(
                        (T) t.merge(t, oldT, threshold)
                );
                // remove it from the old ones
                oldTopics.remove(oldT);
            } else {
                newInterests.add(t);
            }
        }
        // now perform the union between newOnes and oldOnes
        List<T> union = Utils.union(newInterests, oldTopics);
        // if |union| + |merged| > limit, than free some space.
        if(union.size() + merged.size() > getLimit()) {
            Collections.sort(union);
            union = Utils.cut(union, union.size() - merged.size());
        }
        // then add the merged ones
        for(T m : merged) {
            union.add(m);
        }
        return union;
    }

    private int getLimit() {
        return Integer.parseInt(properties.getProperty("interest.limit"));
    }

    private UserProfile toProfile(UUID userId, Collection<Interest> interests, Collection<Category> categories) {
        UserProfile up = new UserProfile(userId);
        up.setInterests(new HashSet<Interest>(interests));
        up.setCategories(new HashSet<Category>(categories));
        up.setVisibility(UserProfile.Visibility.PUBLIC);
        up.setLastUpdated(DateTime.now());
        return up;
    }

    private List<Interest> setIdAndWeight(UUID activityId, Collection<Interest> newInterests, double multiplier) {
        normalize(newInterests);
        for (Interest i : newInterests) {
            i.addActivity(activityId);
            i.setVisible(true);
            i.setWeight((i.getWeight() * multiplier));
        }
        return new ArrayList<Interest>(newInterests);
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
            LOGGER.error(errMsg, e);
            throw new ProfilerException(errMsg, e);
        }
        try {
            return constructor.newInstance(object, nlpEngine, linkEng);
        } catch (Exception e) {
            final String errMsg = "error while instantiating a [" + ruleClass.getName() + "] with type [" + type.getName() + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilerException(errMsg, e);
        }
    }

}
