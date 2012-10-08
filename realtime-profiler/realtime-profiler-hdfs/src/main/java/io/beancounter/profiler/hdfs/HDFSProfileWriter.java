package io.beancounter.profiler.hdfs;

import io.beancounter.commons.model.UserProfile;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.DFSClient;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class HDFSProfileWriter implements ProfileWriter {

    private DistributedFileSystem dfs;

    private Configuration configuration;

    private ObjectMapper mapper = new ObjectMapper();

    public HDFSProfileWriter() throws URISyntaxException, IOException {
        dfs = new DistributedFileSystem();
        configuration = new Configuration();
        configuration.set("fs.default.name", "54.247.87.19:9000");
        configuration.set("mapred.job.tracker", "54.247.108.254:9001");
        configuration.set("dfs.replication", "1");
        configuration.set("dfs.data.dir", "/tmp/hadoop-dpalmisano/dfs/data");
        configuration.set("dfs.name.dir", "/tmp/hadoop-dpalmisano/dfs/name");
        configuration.set("dfs.support.append", "true");
    }

    public void init() throws ProfileWriterException {
        try {
            dfs.initialize(new URI("hdfs://10.224.86.144:9000"), configuration);
        } catch (IOException e) {
            throw new ProfileWriterException("Error while initializing HDFS", e);
        } catch (URISyntaxException e) {
            throw new ProfileWriterException("Error while initializing HDFS", e);
        }
    }

    public void close() throws ProfileWriterException {
        DFSClient client = dfs.getClient();
        try {
            client.close();
        } catch (IOException e) {
            throw new ProfileWriterException("Error while closing HDFS client", e);
        }
        try {
            dfs.close();
        } catch (IOException e) {
            throw new ProfileWriterException("Error while closing HDFS", e);
        }
    }

    public void write(UUID application, UserProfile profile) throws ProfileWriterException {
        UUID userId = profile.getUserId();
        DFSClient client = dfs.getClient();
        String jsonProfile = getJsonRepresentation(profile);
        if(checkIfApplicationDirExists(client, application)) {
            // it means this app already has produced at least one profile
            if(checkIfUserFileExists(client, application, userId)) {
                // ok, this users has been already profiled once at least
                String filename = "/" + application + "/" + userId;
                append(client, filename, jsonProfile);
                return;
            }
            // uhm, ok we should create the file from scratch
            String filename = "/" + application + "/" + userId;
            write(client, filename, jsonProfile);
            return;
        } else {
            // ok, the app dir does not exist
            String applicationDir = createApplicationDir(client, application);
            String filename = "/" + applicationDir + "/" + userId;
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
        pw.write("\n");
        pw.close();
        try {
            os.close();
        } catch (IOException e) {
            throw new ProfileWriterException("Error while closing stream to file [" + filename + "] on HDFS", e);
        }
    }

    private void append(DFSClient client, String filename, String jsonProfile) throws ProfileWriterException {
        OutputStream os;
        try {
            os = client.append(filename, Integer.MAX_VALUE, null, null);
        } catch (IOException e) {
            throw new ProfileWriterException("Error while opening file [" + filename + "] on HDFS", e);
        }
        PrintWriter pw = new PrintWriter(os);
        pw.append(jsonProfile);
        pw.append("\n");
        pw.close();
        try {
            os.close();
        } catch (IOException e) {
            throw new ProfileWriterException("Error while closing stream to file [" + filename + "] on HDFS", e);
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