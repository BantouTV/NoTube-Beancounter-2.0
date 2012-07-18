package tv.notube.commons.redirects;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class RedirectException extends Exception {
    public RedirectException(String message, Exception e) {
        super(message, e);
    }

    public RedirectException(String errMsg) {
        super(errMsg);
    }
}