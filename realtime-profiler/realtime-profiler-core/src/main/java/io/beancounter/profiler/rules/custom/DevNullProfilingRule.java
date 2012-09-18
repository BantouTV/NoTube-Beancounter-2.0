package io.beancounter.profiler.rules.custom;

import io.beancounter.commons.linking.LinkingEngine;
import io.beancounter.commons.model.Interest;
import io.beancounter.commons.model.activity.Object;
import io.beancounter.commons.nlp.NLPEngine;
import io.beancounter.profiler.rules.ObjectProfilingRule;
import io.beancounter.profiler.rules.ProfilingRuleException;

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

    private final Set<Interest> result = new HashSet<Interest>();

    public DevNullProfilingRule() {
        this(null, null, null);
    }

    private DevNullProfilingRule(Object object, NLPEngine nlpEngine, LinkingEngine linkingEngine) {
        super(object, nlpEngine, linkingEngine);
    }

    @Override
    public void run(Properties properties) throws ProfilingRuleException {}

    @Override
    public Collection<Interest> getResult() throws ProfilingRuleException {
        return result;
    }
}
