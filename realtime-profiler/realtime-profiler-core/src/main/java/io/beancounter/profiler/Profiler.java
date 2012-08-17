package io.beancounter.profiler;

import java.util.UUID;

import io.beancounter.commons.linking.LinkingEngine;
import io.beancounter.commons.model.UserProfile;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.Verb;
import io.beancounter.commons.nlp.NLPEngine;
import io.beancounter.profiler.rules.ObjectProfilingRule;
import io.beancounter.profiler.rules.ProfilingRule;
import io.beancounter.profiles.Profiles;

/**
 * This interface models the minimum functionalities of a <i>beancounter.io</i>
 * realtime profiler.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface Profiler {

    public void registerRule(
            Class<? extends io.beancounter.commons.model.activity.Object> type,
            Class<? extends ObjectProfilingRule> rule
    ) throws ProfilerException;

    public void registerRule(
            Verb verb,
            ProfilingRule rule
    ) throws ProfilerException;

    public UserProfile profile(UUID userId, Activity activity) throws ProfilerException;

    public Profiles getProfileStore();

    public NLPEngine getNLPEngine();

    public LinkingEngine getLinkingEngine();

}
