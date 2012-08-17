package io.beancounter.profiler.rules.custom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.beancounter.commons.linking.LinkingEngine;
import io.beancounter.commons.model.activity.Object;
import io.beancounter.commons.nlp.*;
import io.beancounter.profiler.rules.ObjectProfilingRule;
import io.beancounter.profiler.rules.ProfilingRuleException;

import java.net.URI;
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

    private static final Logger logger = LoggerFactory.getLogger(GenericObjectProfilingRule.class);

    private Set<URI> result = new HashSet<URI>();

    public GenericObjectProfilingRule(
            io.beancounter.commons.model.activity.Object object,
            NLPEngine nlpEngine,
            LinkingEngine linkingEngine
    ) {
        super(object, nlpEngine, linkingEngine);
    }

    @Override
    public void run(Properties properties) throws ProfilingRuleException {
        Object object = getObject();
        URL url = object.getUrl();
        NLPEngineResult result;
        try {
            result = getNLPEngine().enrich(url);
        } catch (NLPEngineException e) {
            final String errMsg = "error while trying to extract knowledge from [" + url + "]";
            logger.error(errMsg, e);
            throw new ProfilingRuleException(errMsg, e);
        }
        for (Entity entity : result.getEntities()) {
            this.result.add(entity.getResource());
        }
        for (Category category : result.getCategories()) {
            this.result.add(category.getResource());
        }
    }

    @Override
    public Collection<URI> getResult() throws ProfilingRuleException {
        return result;
    }

}