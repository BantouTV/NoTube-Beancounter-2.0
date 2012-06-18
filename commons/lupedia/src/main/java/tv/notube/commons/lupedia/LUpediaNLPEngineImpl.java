package tv.notube.commons.lupedia;

import org.codehaus.jackson.map.ObjectMapper;
import tv.notube.commons.nlp.NLPEngine;
import tv.notube.commons.nlp.NLPEngineException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public final class LUpediaNLPEngineImpl implements NLPEngine {

    public final static String QUERY_PATTERN = "http://lupedia.ontotext.com/lookup/text2json?lookupText=%s&threshold=%s";

    @Override
    public Collection<URI> enrich(String text) throws NLPEngineException {
        URL query;
        // TODO (high) this should be configurable
        String queryStr = String.format(QUERY_PATTERN, text, 0.85);
        try {
            query = new URL(queryStr);
        } catch (MalformedURLException e) {
            throw new NLPEngineException("url [" + queryStr + "] seems to be ill-formed", e);
        }
        InputStream is = getStream(query);
        ObjectMapper mapper = new ObjectMapper();
        List<LUpediaEntity> entities;
        try {
            entities = mapper.readValue(is, List.class);
        } catch (IOException e) {
            throw new NLPEngineException(
                    "error while parsing json from [" + queryStr + "]",
                    e
            );
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new NLPEngineException(
                        "error while closing stream from [" + queryStr + "]",
                        e
                );
            }
        }
        Collection<URI> result = new ArrayList<URI>();
        for(LUpediaEntity entity : entities) {
            result.add(entity.getInstanceUri());
        }
        return result;
    }

    private InputStream getStream(URL query) throws NLPEngineException {
        URLConnection urlConnection;
        try {
            urlConnection = query.openConnection();
        } catch (IOException e) {
            throw new NLPEngineException(
                    "Error while opening connection from [" + query + "]", e
            );
        }
        try {
            urlConnection.connect();
        } catch (IOException e) {
            throw new NLPEngineException(
                    "Error while connecting to [" + query + "]", e
            );
        }
        try {
            return urlConnection.getInputStream();
        } catch (IOException e) {
            throw new NLPEngineException(
                    "Error getting stream from [" + query + "]", e
            );
        }
    }

    @Override
    public Collection<URI> enrich(URL url) throws NLPEngineException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
