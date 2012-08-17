package io.beancounter.profiler;

/**
 * Raised if something goes wrong within {@link Profiler}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ProfilerException extends Exception {

    public ProfilerException(String message, Exception e) {
        super(message, e);
    }
}
