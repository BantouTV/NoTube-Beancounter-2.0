package io.beancounter.usermanager;

/**
 * Most {@link UserManager} exception.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class UserManagerException extends Exception {

    public UserManagerException(String message) {
        super(message);
    }

    public UserManagerException(String message, Exception e) {
        super(message, e);
    }
}
