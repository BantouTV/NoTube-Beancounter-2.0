package io.beancounter.profiler.rules.custom;

import io.beancounter.commons.model.Interest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.beancounter.commons.linking.LinkingEngine;
import io.beancounter.commons.model.activity.Object;
import io.beancounter.commons.nlp.*;
import io.beancounter.profiler.rules.ObjectProfilingRule;
import io.beancounter.profiler.rules.ProfilingRuleException;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * This {@link io.beancounter.profiler.rules.ProfilingRule} extracts interests
 * in terms of <code>URI</code>s from every shared Web page, wrapped in a
 * {@link Object}.
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class GenericObjectProfilingRule
        extends ObjectProfilingRule<io.beancounter.commons.model.activity.Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericObjectProfilingRule.class);

    private Set<Interest> result = new HashSet<Interest>();

    public GenericObjectProfilingRule(
            io.beancounter.commons.model.activity.Object object,
            NLPEngine nlpEngine,
            LinkingEngine linkingEngine
    ) {
        super(object, nlpEngine, linkingEngine);
    }

    @Override
    public void run(Properties properties) throws ProfilingRuleException {
        LOGGER.debug("rule started");
        Object object = getObject();
        URL url = object.getUrl();
        NLPEngineResult result;
        try {
            result = getNLPEngine().enrich(url);
        } catch (NLPEngineException e) {
            final String errMsg = "error while trying to extract knowledge from [" + url + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilingRuleException(errMsg, e);
        }
        this.result.addAll(InterestConverter.convert(result));
        LOGGER.debug("rule ended with {} interests found", this.result.size());
    }

    @Override
    public Collection<Interest> getResult() throws ProfilingRuleException {
        return result;
    }

}