package io.beancounter.commons.linking;

/**
 * Raised if a {@link LinkingEngine} implementation is unable
 * to find any link.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class LinkNotFoundException extends LinkingEngineException {

    public LinkNotFoundException(String message) {
        super(message);
    }

    public LinkNotFoundException(String message, Exception e) {
        super(message, e);
    }
}
