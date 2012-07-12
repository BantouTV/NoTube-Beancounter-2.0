package tv.notube.resolver;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ResolverException extends Exception {

    public ResolverException(String message, Exception e) {
        super(message, e);
    }

    public ResolverException(String message) {
        super(message);
    }

}
