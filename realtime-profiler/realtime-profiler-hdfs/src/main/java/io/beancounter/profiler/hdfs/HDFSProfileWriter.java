package io.beancounter.profiler.hdfs;

import com.google.inject.Inject;
import io.beancounter.commons.model.UserProfile;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.DFSClient;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.codehaus.jackson.map.ObjectMapper;

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
        try {
            dfs.initialize(new URI("hdfs://10.224.86.144:9000"), configuration);
        } catch (Exception e) {
            throw new ProfileWriterException("Error while initializing HDFS", e);
        }
        client = dfs.getClient();
    }

    @Override
    public void close() throws ProfileWriterException {
        try {
            client.close();
        } catch (IOException e) {
            throw new ProfileWriterException("Error while client to HDFS", e);
        }
        try {
            dfs.close();
        } catch (IOException e) {
            throw new ProfileWriterException("Error while closing HDFS", e);
        }
    }

    @Override
    public synchronized void write(UUID application, UserProfile profile) throws ProfileWriterException {
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
    }

    private String createApplicationDir(DFSClient client, UUID application) throws ProfileWriterException {
        try {
            client.mkdirs("/" + application);
        } catch (IOException e) {
            throw new ProfileWriterException("Error while creating directory [" + application + "] on HDFS", e);
        }
        return application.toString();
    }

    private void write(DFSClient client, String filename, String jsonProfile) throws ProfileWriterException {
        OutputStream os;
        try {
            os = client.create(filename, true);
        } catch (IOException e) {
            throw new ProfileWriterException("Error while creating file [" + filename + "] on HDFS", e);
        }
        PrintWriter pw = new PrintWriter(os);
        pw.write(jsonProfile);
        pw.println();
        pw.close();
        if (pw.checkError()) {
            throw new ProfileWriterException("Error while closing writer to file [" + filename + "] on HDFS");
        }
        try {
            os.close();
        } catch (IOException e) {
            throw new ProfileWriterException("Error while closing stream to file [" + filename + "] on HDFS");
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
            throw new ProfileWriterException("Error while closing writer to file [" + filename + "] on HDFS");
        }
        try {
            os.close();
        } catch (IOException e) {
            throw new ProfileWriterException("Error while closing stream from file [" + filename + "] on HDFS", e);
        }
    }

    private String getJsonRepresentation(UserProfile profile) throws ProfileWriterException {
        try {
            return mapper.writeValueAsString(profile);
        } catch (IOException e) {
            throw new ProfileWriterException("error while serializing [" + profile +  "] to JSON", e);
        }
    }

    private boolean checkIfApplicationDirExists(DFSClient client, UUID application) throws ProfileWriterException {
        try {
            return client.exists("/" + application);
        } catch (IOException e) {
            throw new ProfileWriterException("Error while checking if directory [" + application + "] exists", e);
        }
    }

    private boolean checkIfUserFileExists(DFSClient client, UUID application, UUID userId) throws ProfileWriterException {
        try {
            return client.exists("/" + application + "/" + userId);
        } catch (IOException e) {
            throw new ProfileWriterException("Error while checking if file [" + userId + "] exists", e);
        }
    }
}