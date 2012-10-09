package io.beancounter.profiler.hdfs;

import io.beancounter.commons.model.UserProfile;

import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface ProfileWriter {

    public void init() throws ProfileWriterException;

    public void write(UUID application, UserProfile profile) throws ProfileWriterException;

    public void close() throws ProfileWriterException;

}
