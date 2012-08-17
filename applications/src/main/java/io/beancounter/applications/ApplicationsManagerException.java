package io.beancounter.applications;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ApplicationsManagerException extends Exception {

    public ApplicationsManagerException(String message, Exception e) {
        super(message, e);
    }

    public ApplicationsManagerException(String message) {
        super(message);
    }
}
