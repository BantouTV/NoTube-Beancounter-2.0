package io.beancounter.profiler.rules.custom;

import io.beancounter.commons.model.*;
import io.beancounter.commons.model.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.beancounter.commons.linking.LinkingEngine;
import io.beancounter.commons.model.activity.Object;
import io.beancounter.commons.nlp.*;
import io.beancounter.profiler.rules.ObjectProfilingRule;
import io.beancounter.profiler.rules.ProfilingRuleException;

import java.net.URL;
import java.util.*;

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

    private Set<Interest> interests = new HashSet<Interest>();

    private Set<io.beancounter.commons.model.Category> categories =
            new HashSet<io.beancounter.commons.model.Category>();

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
        interests.addAll(InterestConverter.toInterests(result.getEntities()));
        categories.addAll(InterestConverter.toCategories(result.getCategories()));
        LOGGER.debug("rule ended with {} interests found", this.interests.size());
    }

    @Override
    public List<Interest> getInterests() throws ProfilingRuleException {
        return new ArrayList<Interest>(interests);
    }

    @Override
    public List<Category> getCategories()
            throws ProfilingRuleException {
        return new ArrayList<Category>(categories);
    }

}