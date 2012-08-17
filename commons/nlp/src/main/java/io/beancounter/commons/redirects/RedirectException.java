package io.beancounter.commons.redirects;

/**
 * This exception is raised if something goes wrong with
 * {@link RedirectResolver}.
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class RedirectException extends Exception {

    public RedirectException(String message, Exception e) {
        super(message, e);
    }

    public RedirectException(String message) {
        super(message);
    }
}