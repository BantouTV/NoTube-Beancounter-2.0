package tv.notube.profiler.rules.custom;

import tv.notube.commons.linking.LinkingEngine;
import tv.notube.commons.model.activity.Object;
import tv.notube.commons.nlp.*;
import tv.notube.profiler.rules.ObjectProfilingRule;
import tv.notube.profiler.rules.ProfilingRuleException;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class FacebookProfilingRule extends ObjectProfilingRule<tv.notube.commons.model.activity.Object> {

    private Set<URI> result = new HashSet<URI>();

    public FacebookProfilingRule(tv.notube.commons.model.activity.Object object, NLPEngine nlpEngine, LinkingEngine linkingEngine) {
        super(object, nlpEngine, linkingEngine);
    }

    @Override
    public void run(Properties properties) throws ProfilingRuleException {
        Object object = getObject();
        result.addAll(
                getResources(splitCategories(object.getDescription()))
        );
        if(isNotFacebook(object.getUrl())) {
            result.addAll(getResources(object.getUrl()));
        }
    }

    @Override
    public Collection<URI> getResult() throws ProfilingRuleException {
        return result;
    }

    private Collection<URI> getResources(String text) throws ProfilingRuleException {
        NLPEngineResult nlpResult;
        try {
            nlpResult = getNLPEngine().enrich(text);
        } catch (NLPEngineException e) {
            throw new ProfilingRuleException(
                    "Error while extracting interests from text [" + text + "]",
                    e
            );
        }
        return toURICollection(nlpResult);
    }


    private Collection<URI> toURICollection(NLPEngineResult nlpResult) {
        Collection<URI> result = new HashSet<URI>();
        for(Entity entity : nlpResult.getEntities()) {
            result.add(entity.getResource());
        }
        for(Category category : nlpResult.getCategories()) {
            result.add(category.getResource());
        }
        return result;
    }

    private Collection<URI> getResources(URL url) throws ProfilingRuleException {
        NLPEngineResult nlpResult;
        try {
            nlpResult = getNLPEngine().enrich(url);
        } catch (NLPEngineException e) {
            throw new ProfilingRuleException(
                    "Error while extracting interests from url [" + url + "]",
                    e
            );
        }
        return toURICollection(nlpResult);
    }

    private boolean isNotFacebook(URL url) {
        String urlStr = url.getHost();
        String[] array = urlStr.split("\\.");
        for(int i = 0; i<array.length; i++) {
            if(array[i].equals("facebook")) {
                return false;
            }
        }
        return true;
    }

    private String splitCategories(String description) {
        return description.replaceAll("/", " ");
    }

}