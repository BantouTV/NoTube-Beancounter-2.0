package io.beancounter.commons.helper.reflection;

/**
 * Raised if something goes wrong with {@link ReflectionHelper}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ReflectionHelperException extends Exception {

    public ReflectionHelperException(String message, Exception e) {
        super(message, e);
    }
}
