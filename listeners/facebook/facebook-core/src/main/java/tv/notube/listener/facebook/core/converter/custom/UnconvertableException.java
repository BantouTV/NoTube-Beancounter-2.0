package tv.notube.listener.facebook.core.converter.custom;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class UnconvertableException extends ConverterException {

    public UnconvertableException(String message) {
        super(message);
    }

    public UnconvertableException(String message, Exception e) {
        super(message, e);
    }
}
