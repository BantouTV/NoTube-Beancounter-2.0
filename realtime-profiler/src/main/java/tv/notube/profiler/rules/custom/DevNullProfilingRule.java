package tv.notube.profiler.rules.custom;

import tv.notube.commons.linking.LinkingEngine;
import tv.notube.commons.nlp.NLPEngine;
import tv.notube.profiler.rules.ObjectProfilingRule;
import tv.notube.profiler.rules.ProfilingRuleException;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * It simulates a <i>/dev/null</i>
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class DevNullProfilingRule extends ObjectProfilingRule<tv.notube.commons.model.activity.Object> {

    private Set<URI> result = new HashSet<URI>();

    public DevNullProfilingRule(tv.notube.commons.model.activity.Object object, NLPEngine nlpEngine, LinkingEngine linkingEngine) {
        super(object, nlpEngine, linkingEngine);
    }

    @Override
    public void run(Properties properties) throws ProfilingRuleException {}

    @Override
    public Collection<URI> getResult() throws ProfilingRuleException {
        return result;
    }
}
