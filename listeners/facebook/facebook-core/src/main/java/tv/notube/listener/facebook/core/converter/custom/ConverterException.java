package tv.notube.listener.facebook.core.converter.custom;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ConverterException extends Exception {

    public ConverterException(String message) {
        super(message);
    }

    public ConverterException(String message, Exception e) {
        super(message, e);
    }
}
