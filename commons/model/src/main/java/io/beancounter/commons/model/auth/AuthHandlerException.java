package io.beancounter.commons.model.auth;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class AuthHandlerException extends Exception {

    public AuthHandlerException(String message) {
        super(message);
    }

    public AuthHandlerException(String message, Exception e) {
        super(message, e);
    }

}
