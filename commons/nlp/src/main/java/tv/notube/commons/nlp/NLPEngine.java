package tv.notube.commons.nlp;

import java.net.URL;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface NLPEngine {

    public NLPEngineResult enrich(String text) throws NLPEngineException;

    public NLPEngineResult enrich(URL url) throws NLPEngineException;

}
