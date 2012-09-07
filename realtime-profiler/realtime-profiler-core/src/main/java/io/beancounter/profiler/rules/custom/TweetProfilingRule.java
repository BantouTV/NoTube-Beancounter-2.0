package io.beancounter.profiler.rules.custom;

import io.beancounter.commons.linking.LinkingEngine;
import io.beancounter.commons.model.Interest;
import io.beancounter.commons.model.activity.Tweet;
import io.beancounter.commons.nlp.NLPEngine;
import io.beancounter.commons.nlp.NLPEngineException;
import io.beancounter.commons.nlp.NLPEngineResult;
import io.beancounter.commons.tagdef.TagDef;
import io.beancounter.commons.tagdef.TagDefException;
import io.beancounter.profiler.rules.ProfilingRuleException;
import io.beancounter.profiler.rules.ObjectProfilingRule;

import java.net.URL;
import java.util.*;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
// TODO: make a better use of the new stuff coming from NLPengine
public class TweetProfilingRule extends ObjectProfilingRule<Tweet> {

    private Set<Interest> result = new HashSet<Interest>();

    public TweetProfilingRule(Tweet tweet, NLPEngine nlpEngine, LinkingEngine linkingEngine) {
        super(tweet, nlpEngine, linkingEngine);
    }

    public void run(Properties properties) throws ProfilingRuleException {
        Tweet tweet = getObject();
        // grab interests from pure tweet text
        result.addAll(
                    getResources(tweet.getText())
            );
        // grab interests from urls eventually contained in the tweet
        for (URL url : tweet.getUrls()) {
            result.addAll(getResources(url));
        }
        // get resources from tweet eventual hashtags if enabled
        if (properties.getProperty("tagdef.enable").equals("true")) {
            for (String hashTag : tweet.getHashTags()) {
                result.addAll(getResourcesFromHashTag(hashTag));
            }
        }
    }

    private Collection<Interest> getResources(String text) throws ProfilingRuleException {
        NLPEngineResult nlpResult;
        try {
            nlpResult = getNLPEngine().enrich(text);
        } catch (NLPEngineException e) {
            throw new ProfilingRuleException(
                    "Error while extracting interests from text [" + text + "]",
                    e
            );
        }
        return InterestConverter.convert(nlpResult);
    }

    private Collection<Interest> getResources(URL url) throws ProfilingRuleException {
        NLPEngineResult nlpResult;
        try {
            nlpResult = getNLPEngine().enrich(url);
        } catch (NLPEngineException e) {
            throw new ProfilingRuleException(
                    "Error while extracting interests from url [" + url + "]",
                    e
            );
        }
        return InterestConverter.convert(nlpResult);
    }

    private Collection<Interest> getResourcesFromHashTag(String hashTag) throws ProfilingRuleException {
        TagDef tagDef = new TagDef();
        List<String> defs;
        try {
            defs = tagDef.getDefinitions(hashTag);
        } catch (TagDefException e) {
            throw new ProfilingRuleException(
                    "Error while accessing to TagDef for '" + hashTag + "'",
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
                throw new ProfilingRuleException(
                        "Error while extracting interests from #hashtag '" + hashTag + "'",
                        e
                );
            }
        }
        return resources;
    }

    public Collection<Interest> getResult() throws ProfilingRuleException {
        return result;
    }

}
