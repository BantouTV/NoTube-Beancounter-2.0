package io.beancounter.profiler.rules.custom;

import io.beancounter.commons.linking.LinkingEngine;
import io.beancounter.commons.model.activity.Object;
import io.beancounter.commons.nlp.NLPEngine;
import io.beancounter.profiler.rules.ObjectProfilingRule;
import io.beancounter.profiler.rules.ProfilingRuleException;

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
public class DevNullProfilingRule extends ObjectProfilingRule<Object> {

    private final Set<URI> result = new HashSet<URI>();

    public DevNullProfilingRule() {
        this(null, null, null);
    }

    private DevNullProfilingRule(Object object, NLPEngine nlpEngine, LinkingEngine linkingEngine) {
        super(object, nlpEngine, linkingEngine);
    }

    @Override
    public void run(Properties properties) throws ProfilingRuleException {}

    @Override
    public Collection<URI> getResult() throws ProfilingRuleException {
        return result;
    }
}
