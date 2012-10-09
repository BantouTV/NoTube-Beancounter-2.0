package io.beancounter.profiler.hdfs;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ProfileWriterException extends Exception {

    public ProfileWriterException(String message) {
        super(message);
    }

    public ProfileWriterException(String message, Exception e) {
        super(message, e);
    }
}
