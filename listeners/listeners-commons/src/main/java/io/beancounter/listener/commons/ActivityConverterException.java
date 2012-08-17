package io.beancounter.listener.commons;

/**
 * Raised if something goes wrong within {@link ActivityConverter}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ActivityConverterException extends Exception {

    public ActivityConverterException(String message) {
        super(message);
    }

    public ActivityConverterException(String message, Exception e) {
        super(message, e);
    }
}
