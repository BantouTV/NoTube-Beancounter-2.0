package tv.notube.commons.lupedia;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import tv.notube.commons.nlp.Entity;
import tv.notube.commons.nlp.NLPEngine;
import tv.notube.commons.nlp.NLPEngineException;
import tv.notube.commons.nlp.NLPEngineResult;
import tv.notube.commons.redirects.RedirectException;
import tv.notube.commons.redirects.RedirectResolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public final class LUpediaNLPEngineImpl implements NLPEngine {

    public final static String QUERY_PATTERN = "http://lupedia.ontotext.com/lookup/text2json?threshold=%s";

    @Override
    public NLPEngineResult enrich(String text) throws NLPEngineException {
        try {
            text = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 is not supported on this platform", e);
        }
        URL query;
        // TODO (high) this should be configurable
        String queryStr = String.format(QUERY_PATTERN, 0.85);
        try {
            query = new URL(queryStr);
        } catch (MalformedURLException e) {
            throw new NLPEngineException("url [" + queryStr + "] seems to be ill-formed", e);
        }
        PostMethod postMethod = new PostMethod(queryStr);
        postMethod.setParameter("lookupText", text);
        HttpClient client = new HttpClient();
        InputStream is;
        try {
            client.executeMethod(postMethod);
            is = postMethod.getResponseBodyAsStream();
        } catch (IOException e) {
            throw new NLPEngineException("error opening the connection for [" + queryStr + "]", e);
        }
        ObjectMapper mapper = new ObjectMapper();
        List<LUpediaEntity> entities;
        try {
            entities = mapper.readValue(is, TypeFactory.defaultInstance().constructCollectionType(List.class, LUpediaEntity.class));
        } catch (IOException e) {
            throw new NLPEngineException(
                    "error while parsing json from [" + queryStr + "]",
                    e
            );
        } finally {
            postMethod.releaseConnection();
            try {
                is.close();
            } catch (IOException e) {
                throw new NLPEngineException(
                        "error while closing stream from [" + query.toString() + "]",
                        e
                );
            }
        }
        NLPEngineResult result = new NLPEngineResult();
        for(LUpediaEntity entity : entities) {
            String candidateLabel = getURILastPart(entity.getInstanceUri());
            Entity e = Entity.build(
                    entity.getInstanceUri(),
                    candidateLabel
            );
            e.setType(getURILastPart(entity.getInstanceClass()));
            result.addEntity(e);
        }
        return result;
    }

    private String getURILastPart(String uri) {
        URI uriObj;
        try {
            uriObj = new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        String path = uriObj.getPath();
        String[] pathParts = path.split("/");
        return pathParts[pathParts.length - 1];
    }

    @Override
    public NLPEngineResult enrich(URL url) throws NLPEngineException {
        String response;
        try {
            response = RedirectResolver.resolve(url);
        } catch (RedirectException e) {
            throw new NLPEngineException(
                    "Error resolving the redirect for [" + url + "]",
                    e
            );
        }
        String text;
        try {
            text = ArticleExtractor.INSTANCE.getText(response);
        } catch (BoilerpipeProcessingException e) {
            throw new NLPEngineException("Error while removing boiler plate from [" + url +  "]", e);
        }
        return enrich(text);
    }
}