package io.beancounter.profiler.rules.custom;

import io.beancounter.commons.model.Interest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.beancounter.commons.linking.LinkNotFoundException;
import io.beancounter.commons.linking.LinkingEngine;
import io.beancounter.commons.linking.LinkingEngineException;
import io.beancounter.commons.model.activity.facebook.Like;
import io.beancounter.commons.nlp.*;
import io.beancounter.profiler.rules.ObjectProfilingRule;
import io.beancounter.profiler.rules.ProfilingRuleException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * This {@link io.beancounter.profiler.rules.ProfilingRule} implementation is able
 * to extract interests from <i>Facebook Likes</i>.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FacebookLikeProfilingRule extends ObjectProfilingRule<Like> {

    private static final Logger logger = LoggerFactory.getLogger(FacebookLikeProfilingRule.class);

    private final static String BASE_URI = "http://dati.rai.tv/";

    private final static String RELEVANT ="category/";

    private Set<Interest> result = new HashSet<Interest>();

    public FacebookLikeProfilingRule(
            Like like,
            NLPEngine nlpEngine,
            LinkingEngine linkingEngine
    ) {
        super(like, nlpEngine, linkingEngine);
    }

    @Override
    public void run(Properties properties) throws ProfilingRuleException {
        Like like = getObject();
        for(String category : like.getCategories()) {
            String cogitoCategory;
            try {
                cogitoCategory = getLinkingEngine().link(category);
            } catch (LinkNotFoundException e) {
                final String errMsg = "could not find any link from [" + category + "]. Skipping.";
                logger.error(errMsg, e);
                continue;
            }
            catch (LinkingEngineException e) {
                final String errMsg = "error while trying to link [" + category + "]";
                logger.error(errMsg, e);
                throw new ProfilingRuleException(errMsg, e);
            }
            try {
                result.add(
                        new Interest(cogitoCategory, new URI(BASE_URI + RELEVANT + cogitoCategory))
                );
            } catch (URISyntaxException e) {
                final String errMsg = "error while trying to link [" + category + "]";
                logger.error(errMsg, e);
                throw new RuntimeException(errMsg, e);
            }
        }
    }

    @Override
    public Collection<Interest> getResult() throws ProfilingRuleException {
        return result;
    }

}