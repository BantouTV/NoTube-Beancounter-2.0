package io.beancounter.profiler.hdfs;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ProfileWriterException extends Exception {

    public ProfileWriterException(String message, Exception e) {
        super(message, e);
    }
}
