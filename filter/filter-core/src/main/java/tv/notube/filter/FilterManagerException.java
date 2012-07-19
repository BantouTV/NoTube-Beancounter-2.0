package tv.notube.filter;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FilterManagerException extends Exception {

    public FilterManagerException(String message) {
        super(message);
    }

    public FilterManagerException(String message, Exception e) {
        super(message, e);
    }
}
