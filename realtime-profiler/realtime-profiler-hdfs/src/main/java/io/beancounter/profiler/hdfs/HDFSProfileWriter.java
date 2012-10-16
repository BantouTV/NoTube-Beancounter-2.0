package io.beancounter.profiler.hdfs;

import com.google.inject.Inject;
import io.beancounter.commons.model.UserProfile;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.DFSClient;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class HDFSProfileWriter implements ProfileWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfilerWriterRoute.class);

    static final int BUFFER_SIZE = 4096;

    private DistributedFileSystem dfs;

    private DFSClient client;

    private Configuration configuration;

    private ObjectMapper mapper;

    @Inject
    public HDFSProfileWriter(DistributedFileSystem dfs, Configuration configuration) {
        this.dfs = dfs;
        this.configuration = configuration;
        mapper = new ObjectMapper();
    }

    @Override
    public void init() throws ProfileWriterException {
        LOGGER.debug("inizializing connection to HDFS");
        try {
            // TODO (med): This should be configurable.
            dfs.initialize(new URI("hdfs://10.224.86.144:9000"), configuration);
        } catch (Exception e) {
            final String errMsg = "Error while initializing HDFS";
            LOGGER.error(errMsg, e);
            throw new ProfileWriterException(errMsg, e);
        }
        client = dfs.getClient();
    }

    @Override
    public void close() throws ProfileWriterException {
        try {
            client.close();
        } catch (IOException e) {
            final String errMsg = "Error while closing client to HDFS";
            LOGGER.error(errMsg, e);
            throw new ProfileWriterException(errMsg, e);
        }
        try {
            dfs.close();
        } catch (IOException e) {
            final String errMsg = "Error while closing HDFS";
            LOGGER.error(errMsg, e);
            throw new ProfileWriterException(errMsg, e);
        }
    }

    @Override
    public synchronized void write(UUID application, UserProfile profile) throws ProfileWriterException {
        LOGGER.debug("write to HDFS started");
        UUID userId = profile.getUserId();
        String jsonProfile = getJsonRepresentation(profile);

        if (!checkIfApplicationDirExists(client, application)) {
            // ok, the app dir does not exist
            createApplicationDir(client, application);
        }

        String filename = "/" + application + "/" + userId;

        if (checkIfUserFileExists(client, application, userId)) {
            // ok, this users has been already profiled once at least
            append(client, filename, jsonProfile);
        } else {
            // uhm, ok we should create the file from scratch
            write(client, filename, jsonProfile);
        }
        LOGGER.debug("write to HDFS ended");
    }

    private String createApplicationDir(DFSClient client, UUID application) throws ProfileWriterException {
        try {
            client.mkdirs("/" + application);
        } catch (IOException e) {
            final String errMsg = "Error while creating directory [" + application + "] on HDFS";
            LOGGER.error(errMsg, e);
            throw new ProfileWriterException(errMsg, e);
        }
        return application.toString();
    }

    private void write(DFSClient client, String filename, String jsonProfile) throws ProfileWriterException {
        OutputStream os;
        try {
            os = client.create(filename, true);
        } catch (IOException e) {
            final String errMsg = "Error while creating file [" + filename + "] on HDFS";
            LOGGER.error(errMsg, e);
            throw new ProfileWriterException(errMsg, e);
        }
        // TODO (med): Consider replacing PrintWriter with something more
        // verbose and low-level - we don't want exceptions to be suppressed.
        // Also, having to close the OutputStream twice seems dodgy.
        PrintWriter pw = new PrintWriter(os);
        pw.write(jsonProfile);
        pw.println();
        pw.close();
        if (pw.checkError()) {
            final String errMsg = "Error while closing writer to file [" + filename + "] on HDFS";
            LOGGER.error(errMsg);
            throw new ProfileWriterException(errMsg);
        }
        try {
            os.close();
        } catch (IOException e) {
            final String errMsg = "Error while closing stream to file [" + filename + "] on HDFS";
            LOGGER.error(errMsg);
            throw new ProfileWriterException(errMsg);
        }
    }

    private void append(DFSClient client, String filename, String jsonProfile) throws ProfileWriterException {
        OutputStream os;
        try {
            os = client.append(filename, BUFFER_SIZE, null, null);
        } catch (IOException e) {
            throw new ProfileWriterException("Error while opening file [" + filename + "] on HDFS", e);
        }
        PrintWriter pw = new PrintWriter(os);
        pw.append(jsonProfile);
        pw.println();
        pw.close();
        if (pw.checkError()) {
            final String errMsg = "Error while closing writer to file [" + filename + "] on HDFS";
            LOGGER.error(errMsg);
            throw new ProfileWriterException(errMsg);
        }
        try {
            os.close();
        } catch (IOException e) {
            final String errMsg = "Error while closing stream from file [" + filename + "] on HDFS";
            LOGGER.error(errMsg);
            throw new ProfileWriterException(errMsg, e);
        }
    }

    private String getJsonRepresentation(UserProfile profile) throws ProfileWriterException {
        try {
            return mapper.writeValueAsString(profile);
        } catch (IOException e) {
            final String errMsg = "error while serializing [" + profile +  "] to JSON";
            LOGGER.error(errMsg);
            throw new ProfileWriterException(errMsg, e);
        }
    }

    private boolean checkIfApplicationDirExists(DFSClient client, UUID application) throws ProfileWriterException {
        try {
            return client.exists("/" + application);
        } catch (IOException e) {
            final String errMsg = "Error while checking if directory [" + application + "] exists";
            LOGGER.error(errMsg);
            throw new ProfileWriterException(errMsg, e);
        }
    }

    private boolean checkIfUserFileExists(DFSClient client, UUID application, UUID userId) throws ProfileWriterException {
        try {
            return client.exists("/" + application + "/" + userId);
        } catch (IOException e) {
            final String errMsg = "Error while checking if file [" + userId + "] exists";
            LOGGER.error(errMsg);
            throw new ProfileWriterException(errMsg, e);
        }
    }
}