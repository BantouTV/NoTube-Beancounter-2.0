package io.beancounter.listener.facebook.core.converter;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FacebookActivityConverterException extends Exception {

    public FacebookActivityConverterException(String message) {
        super(message);
    }

    public FacebookActivityConverterException(String message, Exception e) {
        super(message, e);
    }
}
