package io.beancounter.usermanager.services.auth;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ServiceAuthorizationManagerException extends Exception {

    public ServiceAuthorizationManagerException(String message, Exception e) {
        super(message, e);
    }

    public ServiceAuthorizationManagerException(String message) {
        super(message);
    }
}
