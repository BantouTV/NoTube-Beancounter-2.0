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

    public NLPEngineResult enrich(String text) throws NLPEngineException;

    public NLPEngineResult enrich(URL url) throws NLPEngineException;

}
