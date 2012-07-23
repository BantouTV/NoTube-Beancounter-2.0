package tv.notube.profiler.rules.custom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tv.notube.commons.linking.LinkNotFoundException;
import tv.notube.commons.linking.LinkingEngine;
import tv.notube.commons.linking.LinkingEngineException;
import tv.notube.commons.model.activity.facebook.Like;
import tv.notube.commons.nlp.*;
import tv.notube.profiler.rules.ObjectProfilingRule;
import tv.notube.profiler.rules.ProfilingRuleException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * This {@link tv.notube.profiler.rules.ProfilingRule} implementation is able
 * to extract interests from <i>Facebook Likes</i>.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FacebookLikeProfilingRule extends ObjectProfilingRule<Like> {

    private static final Logger logger = LoggerFactory.getLogger(FacebookLikeProfilingRule.class);

    private final static String BASE_URI = "http://dati.rai.tv/";

    private final static String RELEVANT ="category/";

    private Set<URI> result = new HashSet<URI>();

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
                        new URI(BASE_URI + RELEVANT + cogitoCategory)
                );
            } catch (URISyntaxException e) {
                final String errMsg = "error while trying to link [" + category + "]";
                logger.error(errMsg, e);
                throw new RuntimeException(errMsg, e);
            }
        }
    }

    @Override
    public Collection<URI> getResult() throws ProfilingRuleException {
        return result;
    }

}