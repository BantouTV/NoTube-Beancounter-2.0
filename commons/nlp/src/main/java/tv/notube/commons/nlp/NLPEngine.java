package tv.notube.commons.nlp;

import java.net.URI;
import java.net.URL;
import java.util.Collection;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface NLPEngine {

    public Collection<URI> enrich(String text) throws NLPEngineException;

    public Collection<URI> enrich(URL url) throws NLPEngineException;

}
