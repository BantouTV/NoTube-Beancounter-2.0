package tv.notube.commons.linking;

/**
 * Raised if something goes wrong with {@link LinkingEngine}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class LinkingEngineException extends Exception {

    public LinkingEngineException(String message) {
        super(message);
    }

    public LinkingEngineException(String message, Exception e) {
        super(message, e);
    }
}
