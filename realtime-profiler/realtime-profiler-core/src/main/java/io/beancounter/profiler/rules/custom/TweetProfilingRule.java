package io.beancounter.profiler.rules.custom;

import io.beancounter.commons.linking.LinkingEngine;
import io.beancounter.commons.model.Category;
import io.beancounter.commons.model.Interest;
import io.beancounter.commons.model.activity.Tweet;
import io.beancounter.commons.nlp.NLPEngine;
import io.beancounter.commons.nlp.NLPEngineException;
import io.beancounter.commons.nlp.NLPEngineResult;
import io.beancounter.profiler.rules.ProfilingRuleException;
import io.beancounter.profiler.rules.ObjectProfilingRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
// TODO: make a better use of the new stuff coming from NLPengine
public class TweetProfilingRule extends ObjectProfilingRule<Tweet> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericObjectProfilingRule.class);

    private Set<Interest> interests = new HashSet<Interest>();

    private Set<Category> categories = new HashSet<Category>();

    public TweetProfilingRule(Tweet tweet, NLPEngine nlpEngine, LinkingEngine linkingEngine) {
        super(tweet, nlpEngine, linkingEngine);
    }

    public void run(Properties properties) throws ProfilingRuleException {
        LOGGER.debug("rule started");
        Tweet tweet = getObject();
        // grab interests and categories from pure tweet text
        NLPEngineResult result = process(tweet.getText());
        interests.addAll(InterestConverter.toInterests(result.getEntities()));
        categories.addAll(InterestConverter.toCategories(result.getCategories()));

        // grab interests from urls eventually contained in the tweet
        for (URL url : tweet.getUrls()) {
            result = process(url);
            interests.addAll(InterestConverter.toInterests(result.getEntities()));
            Collection<Category> categories = InterestConverter.toCategories(result.getCategories());
            for(Category category : categories) {
                category.addUrl(url);
            }
            this.categories.addAll(categories);
        }
        /*
        // TODO (disabled) 'cause we can't rely on an external service such as tagdef
        // get resources from tweet eventual hashtags if enabled
        if (properties.getProperty("tagdef.enable").equals("true")) {
            for (String hashTag : tweet.getHashTags()) {
                interests.addAll(getResourcesFromHashTag(hashTag));
            }
        }
        */
        LOGGER.debug("rule ended with {} interests found", interests.size());
    }

    private NLPEngineResult process(URL url) throws ProfilingRuleException {
        try {
            return getNLPEngine().enrich(url);
        } catch (NLPEngineException e) {
            final String errMsg = "Error while extracting interests from text [" + url + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilingRuleException(
                    errMsg,
                    e
            );
        }
    }

    private NLPEngineResult process(String text) throws ProfilingRuleException {
        try {
            return getNLPEngine().enrich(text);
        } catch (NLPEngineException e) {
            final String errMsg = "Error while extracting interests from text [" + text + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilingRuleException(
                    errMsg,
                    e
            );
        }
    }

    /*
    private Collection<Interest> getResourcesFromHashTag(String hashTag) throws ProfilingRuleException {
        TagDef tagDef = new TagDef();
        List<String> defs;
        try {
            defs = tagDef.getDefinitions(hashTag);
        } catch (TagDefException e) {
            final String errMsg = "Error while accessing to TagDef for '" + hashTag + "'";
            LOGGER.error(errMsg, e);
            throw new ProfilingRuleException(
                    errMsg,
                    e
            );
        }
        Collection<Interest> resources = new HashSet<Interest>();
        for (String def : defs) {
            try {
                resources.addAll(
                        InterestConverter.convert(getNLPEngine().enrich(def))
                );
            } catch (NLPEngineException e) {
                final String errMsg = "Error while extracting interests from #hashtag '" + hashTag + "'";
                LOGGER.error(errMsg, e);
                throw new ProfilingRuleException(
                        errMsg,
                        e
                );
            }
        }
        return resources;
    }  */

    @Override
    public List<Interest> getInterests() throws ProfilingRuleException {
        return new ArrayList<Interest>(interests);
    }

    @Override
    public List<Category> getCategories() throws ProfilingRuleException {
        return new ArrayList<Category>(categories);
    }

}
