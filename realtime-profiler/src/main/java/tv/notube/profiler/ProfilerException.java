package tv.notube.profiler;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ProfilerException extends Exception {

    public ProfilerException(String message, Exception e) {
        super(message, e);
    }
}
