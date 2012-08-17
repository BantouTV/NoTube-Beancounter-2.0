package io.beancounter.listener.facebook.core.converter;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class UnconvertableFacebookActivityException extends FacebookActivityConverterException {

    public UnconvertableFacebookActivityException(String message, Exception e) {
        super(message, e);
    }
}
