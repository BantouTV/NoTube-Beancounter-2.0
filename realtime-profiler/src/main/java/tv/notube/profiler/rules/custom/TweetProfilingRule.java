package tv.notube.profiler.rules.custom;

import tv.notube.commons.linking.LinkingEngine;
import tv.notube.commons.model.activity.Tweet;
import tv.notube.commons.nlp.NLPEngine;
import tv.notube.commons.nlp.NLPEngineException;
import tv.notube.commons.tagdef.TagDef;
import tv.notube.commons.tagdef.TagDefException;
import tv.notube.profiler.rules.ProfilingRuleException;
import tv.notube.profiler.rules.ObjectProfilingRule;

import java.net.URI;
import java.net.URL;
import java.util.*;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TweetProfilingRule extends ObjectProfilingRule<Tweet> {

    private Set<URI> result = new HashSet<URI>();

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

    private Collection<URI> getResources(String text) throws ProfilingRuleException {
        try {
            return getNLPEngine().enrich(text);
        } catch (NLPEngineException e) {
            throw new ProfilingRuleException(
                    "Error while extracting interests from text [" + text + "]",
                    e
            );
        }
    }

    private Collection<URI> getResources(URL url) throws ProfilingRuleException {
        try {
            return getNLPEngine().enrich(url);
        } catch (NLPEngineException e) {
            throw new ProfilingRuleException(
                    "Error while extracting interests from url [" + url + "]",
                    e
            );
        }
    }

    private Collection<URI> getResourcesFromHashTag(String hashTag) throws ProfilingRuleException {
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
        Collection<URI> resources = new ArrayList<URI>();
        for (String def : defs) {
            try {
                resources.addAll(
                        getNLPEngine().enrich(def)
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

    public Collection<URI> getResult() throws ProfilingRuleException {
        return result;
    }

}
