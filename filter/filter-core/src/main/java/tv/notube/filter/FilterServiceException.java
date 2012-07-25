package tv.notube.filter;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FilterServiceException extends Exception {

    public FilterServiceException(String message, Exception e) {
        super(message, e);
    }
}
