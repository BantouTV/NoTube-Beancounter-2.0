package tv.notube.commons.nlp;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class NLPEngineException extends Exception {

    public NLPEngineException(String message, Exception e) {
        super(message, e);
    }
}