package tv.notube.profiler;

import tv.notube.commons.linking.LinkingEngine;
import tv.notube.commons.model.UserProfile;
import tv.notube.commons.model.activity.*;
import tv.notube.commons.nlp.NLPEngine;
import tv.notube.profiler.rules.ObjectProfilingRule;
import tv.notube.profiler.rules.ProfilingRule;
import tv.notube.profiler.store.ProfileStore;

import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface Profiler {

    public void registerRule(
            Class<? extends tv.notube.commons.model.activity.Object> type,
            Class<? extends ObjectProfilingRule> rule
    ) throws ProfilerException;

    public void registerRule(
            Verb verb,
            ProfilingRule rule
    ) throws ProfilerException;

    public UserProfile profile(UUID userId, Activity activity) throws ProfilerException;

    public ProfileStore getProfileStore();

    public NLPEngine getNLPEngine();

    public LinkingEngine getLinkingEngine();

}
