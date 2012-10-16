package io.beancounter.profiler.rules.custom;

import io.beancounter.commons.model.Category;
import io.beancounter.commons.model.Interest;
import io.beancounter.commons.nlp.NLPEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.beancounter.commons.linking.LinkNotFoundException;
import io.beancounter.commons.linking.LinkingEngine;
import io.beancounter.commons.linking.LinkingEngineException;
import io.beancounter.commons.model.activity.facebook.Like;
import io.beancounter.profiler.rules.ObjectProfilingRule;
import io.beancounter.profiler.rules.ProfilingRuleException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * This {@link io.beancounter.profiler.rules.ProfilingRule} implementation is able
 * to extract interests from <i>Facebook Likes</i>.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FacebookLikeProfilingRule extends ObjectProfilingRule<Like> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookLikeProfilingRule.class);

    private final static String BASE_URI = "http://dati.rai.tv/";

    private final static String RELEVANT ="category/";

    private Set<Interest> interests = new HashSet<Interest>();

    private Set<Category> categories = new HashSet<Category>();

    public FacebookLikeProfilingRule(
            Like like,
            NLPEngine nlpEngine,
            LinkingEngine linkingEngine
    ) {
        super(like, nlpEngine, linkingEngine);
    }

    @Override
    public void run(Properties properties) throws ProfilingRuleException {
        LOGGER.debug("rule started");
        Like like = getObject();
        for(String category : like.getCategories()) {
            String cogitoCategory;
            try {
                cogitoCategory = getLinkingEngine().link(category);
            } catch (LinkNotFoundException e) {
                final String errMsg = "could not find any link from [" + category + "]. Skipping.";
                LOGGER.error(errMsg, e);
                continue;
            }
            catch (LinkingEngineException e) {
                final String errMsg = "error while trying to link [" + category + "]";
                LOGGER.error(errMsg, e);
                throw new ProfilingRuleException(errMsg, e);
            }
            try {
                // TODO (med): This is where some of the NaNs are coming from.
                // These interests will have a weight of 0. Consider giving
                // them a default weight of 1.
                categories.add(
                        new Category(cogitoCategory, new URI(BASE_URI + RELEVANT + cogitoCategory))
                );
            } catch (URISyntaxException e) {
                final String errMsg = "error while trying to link [" + category + "]";
                LOGGER.error(errMsg, e);
                throw new RuntimeException(errMsg, e);
            }
        }
        LOGGER.debug("rule ended with {} interests found", interests.size());
    }

    @Override
    public List<Interest> getInterests() throws ProfilingRuleException {
        return new ArrayList<Interest>(interests);
    }

    @Override
    public List<Category> getCategories() throws ProfilingRuleException {
        return new ArrayList<Category>(categories);
    }

}