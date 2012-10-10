package io.beancounter.profiler.hdfs;

import io.beancounter.commons.model.UserProfile;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.hdfs.DFSClient;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.UUID;

import static io.beancounter.profiler.hdfs.HDFSProfileWriter.BUFFER_SIZE;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class HDFSProfileWriterTest {

    private ProfileWriter profileWriter;
    private DistributedFileSystem dfs;
    private Configuration configuration;
    private ObjectMapper mapper;

    @BeforeMethod
    public void setUp() throws Exception {
        dfs = mock(DistributedFileSystem.class);
        configuration = new Configuration();
        profileWriter = new HDFSProfileWriter(dfs, configuration);
        mapper = new ObjectMapper();
    }

    @Test
    public void createNewHDFSProfileWriter() throws Exception {
        profileWriter = new HDFSProfileWriter(new DistributedFileSystem(), new Configuration());
        assertNotNull(profileWriter);
    }

    @Test
    public void initializingWriterShouldCreateDFSWithNoErrors() throws Exception {
        URI fileSystemLocation = new URI("hdfs://10.224.86.144:9000");

        profileWriter.init();

        verify(dfs).initialize(fileSystemLocation, configuration);
    }

    @Test(expectedExceptions = ProfileWriterException.class)
    public void givenErrorWhenInitializingThenThrowException() throws Exception {
        URI fileSystemLocation = new URI("hdfs://10.224.86.144:9000");

        doThrow(new IOException()).when(dfs).initialize(fileSystemLocation, configuration);

        profileWriter.init();
    }

    @Test
    public void givenNoErrorsWhenClosingTheWriterThenCloseTheDFS() throws Exception {
        DFSClient client = mock(DFSClient.class);
        when(dfs.getClient()).thenReturn(client);
        profileWriter.init();
        profileWriter.close();
        verify(dfs).close();
    }

    @Test(expectedExceptions = ProfileWriterException.class)
    public void givenErrorWhenClosingTheWriterThenThrowException() throws Exception {
        DFSClient client = mock(DFSClient.class);
        when(dfs.getClient()).thenReturn(client);
        profileWriter.init();
        doThrow(new IOException()).when(dfs).close();
        profileWriter.close();
    }

    @Test
    public void writeUserProfileForNewApplication() throws Exception {
        UUID applicationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserProfile profile = new UserProfile("test-user");
        profile.setUserId(userId);
        OutputStream outputStream = mock(OutputStream.class);
        DFSClient client = mock(DFSClient.class);

        ArgumentCaptor<byte[]> writtenBytesCaptor = ArgumentCaptor.forClass(byte[].class);
        when(dfs.getClient()).thenReturn(client);
        when(client.exists("/" + applicationId)).thenReturn(false);
        when(client.create("/" + applicationId + "/" + userId, true)).thenReturn(outputStream);
        doNothing().when(outputStream).write(writtenBytesCaptor.capture(), anyInt(), anyInt());

        profileWriter.init();
        profileWriter.write(applicationId, profile);

        String actual = new String(writtenBytesCaptor.getValue()).replace("\u0000", "");
        String expected = mapper.writeValueAsString(profile) + "\n";
        assertEquals(actual, expected);

        verify(client).mkdirs("/" + applicationId);
    }

    @Test(expectedExceptions = ProfileWriterException.class)
    public void givenErrorWhenWritingUserProfileForNewApplicationThenThrowException() throws Exception {
        UUID applicationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserProfile profile = new UserProfile("test-user");
        profile.setUserId(userId);
        OutputStream outputStream = mock(OutputStream.class);
        DFSClient client = mock(DFSClient.class);

        when(dfs.getClient()).thenReturn(client);
        when(client.exists("/" + applicationId)).thenReturn(false);
        when(client.create("/" + applicationId + "/" + userId, true)).thenReturn(outputStream);
        doThrow(new IOException()).when(outputStream).write(any(byte[].class), anyInt(), anyInt());

        profileWriter.init();
        profileWriter.write(applicationId, profile);
    }

    @Test(expectedExceptions = ProfileWriterException.class)
    public void givenErrorWhenClosingOutputStreamAfterWritingUserProfileForNewApplicationThenThrowException() throws Exception {
        UUID applicationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserProfile profile = new UserProfile("test-user");
        profile.setUserId(userId);
        OutputStream outputStream = mock(OutputStream.class);
        DFSClient client = mock(DFSClient.class);

        when(dfs.getClient()).thenReturn(client);
        when(client.exists("/" + applicationId)).thenReturn(false);
        when(client.create("/" + applicationId + "/" + userId, true)).thenReturn(outputStream);
        doThrow(new IOException()).when(outputStream).close();

        profileWriter.init();
        profileWriter.write(applicationId, profile);
    }

    @Test
    public void writeUserProfileForExistingApplicationButNewUser() throws Exception {
        UUID applicationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserProfile profile = new UserProfile("test-user");
        profile.setUserId(userId);
        OutputStream outputStream = mock(OutputStream.class);
        DFSClient client = mock(DFSClient.class);

        ArgumentCaptor<byte[]> writtenBytesCaptor = ArgumentCaptor.forClass(byte[].class);
        when(dfs.getClient()).thenReturn(client);
        when(client.exists("/" + applicationId)).thenReturn(true);
        when(client.create("/" + applicationId + "/" + userId, true)).thenReturn(outputStream);
        doNothing().when(outputStream).write(writtenBytesCaptor.capture(), anyInt(), anyInt());

        profileWriter.init();
        profileWriter.write(applicationId, profile);

        String actual = new String(writtenBytesCaptor.getValue()).replace("\u0000", "");
        String expected = mapper.writeValueAsString(profile) + "\n";
        assertEquals(actual, expected);
    }

    @Test(expectedExceptions = ProfileWriterException.class)
    public void givenErrorWhenWritingUserProfileForExistingApplicationThenThrowException() throws Exception {
        UUID applicationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserProfile profile = new UserProfile("test-user");
        profile.setUserId(userId);
        OutputStream outputStream = mock(OutputStream.class);
        DFSClient client = mock(DFSClient.class);

        when(dfs.getClient()).thenReturn(client);
        when(client.exists("/" + applicationId)).thenReturn(true);
        when(client.create("/" + applicationId + "/" + userId, true)).thenReturn(outputStream);
        doThrow(new IOException()).when(outputStream).write(any(byte[].class), anyInt(), anyInt());

        profileWriter.init();
        profileWriter.write(applicationId, profile);
    }

    @Test(expectedExceptions = ProfileWriterException.class)
    public void givenErrorWhenClosingOutputStreamAfterWritingUserProfileForExistingApplicationThenThrowException() throws Exception {
        UUID applicationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserProfile profile = new UserProfile("test-user");
        profile.setUserId(userId);
        OutputStream outputStream = mock(OutputStream.class);
        DFSClient client = mock(DFSClient.class);

        when(dfs.getClient()).thenReturn(client);
        when(client.exists("/" + applicationId)).thenReturn(true);
        when(client.create("/" + applicationId + "/" + userId, true)).thenReturn(outputStream);
        doThrow(new IOException()).when(outputStream).close();

        profileWriter.init();
        profileWriter.write(applicationId, profile);
    }

    @Test
    public void appendUserProfileForExistingApplication() throws Exception {
        UUID applicationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserProfile profile = new UserProfile("test-user");
        profile.setUserId(userId);
        FSDataOutputStream outputStream = mock(FSDataOutputStream.class);
        DFSClient client = mock(DFSClient.class);

        ArgumentCaptor<byte[]> writtenBytesCaptor = ArgumentCaptor.forClass(byte[].class);
        when(dfs.getClient()).thenReturn(client);
        when(client.exists("/" + applicationId)).thenReturn(true);
        when(client.exists("/" + applicationId + "/" + userId)).thenReturn(true);
        when(client.append("/" + applicationId + "/" + userId, BUFFER_SIZE, null, null)).thenReturn(outputStream);
        doNothing().when(outputStream).write(writtenBytesCaptor.capture(), anyInt(), anyInt());

        profileWriter.init();
        profileWriter.write(applicationId, profile);

        String actual = new String(writtenBytesCaptor.getValue()).replace("\u0000", "");
        String expected = mapper.writeValueAsString(profile) + "\n";
        assertEquals(actual, expected);
    }

    @Test(expectedExceptions = ProfileWriterException.class)
    public void givenErrorWhenAppendingUserProfileForExistingApplicationThenThrowException() throws Exception {
        UUID applicationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserProfile profile = new UserProfile("test-user");
        profile.setUserId(userId);
        FSDataOutputStream outputStream = mock(FSDataOutputStream.class);
        DFSClient client = mock(DFSClient.class);

        when(dfs.getClient()).thenReturn(client);
        when(client.exists("/" + applicationId)).thenReturn(true);
        when(client.exists("/" + applicationId + "/" + userId)).thenReturn(true);
        when(client.append("/" + applicationId + "/" + userId, BUFFER_SIZE, null, null)).thenReturn(outputStream);
        doThrow(new IOException()).when(outputStream).write(any(byte[].class), anyInt(), anyInt());

        profileWriter.init();
        profileWriter.write(applicationId, profile);
    }

    @Test(expectedExceptions = ProfileWriterException.class)
    public void givenErrorWhenClosingOutputStreamAfterAppendingUserProfileForExistingApplicationThenThrowException() throws Exception {
        UUID applicationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserProfile profile = new UserProfile("test-user");
        profile.setUserId(userId);
        FSDataOutputStream outputStream = mock(FSDataOutputStream.class);
        DFSClient client = mock(DFSClient.class);

        when(dfs.getClient()).thenReturn(client);
        when(client.exists("/" + applicationId)).thenReturn(true);
        when(client.exists("/" + applicationId + "/" + userId)).thenReturn(true);
        when(client.append("/" + applicationId + "/" + userId, BUFFER_SIZE, null, null)).thenReturn(outputStream);
        doThrow(new IOException()).when(outputStream).close();

        profileWriter.init();
        profileWriter.write(applicationId, profile);
    }

    @Test
    public void appendingNewUserProfileSnapshotToSingleExistingUserProfile() throws Exception {
        UUID applicationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserProfile profile = new UserProfile("test-user");
        profile.setUserId(userId);
        MockFSDataOutputStream outputStream = new MockFSDataOutputStream();
        DFSClient client = mock(DFSClient.class);

        String expected = populateExistingProfiles(outputStream, profile, 1);

        when(dfs.getClient()).thenReturn(client);
        when(client.exists("/" + applicationId)).thenReturn(true);
        when(client.exists("/" + applicationId + "/" + userId)).thenReturn(true);
        when(client.append("/" + applicationId + "/" + userId, BUFFER_SIZE, null, null)).thenReturn(outputStream);

        profileWriter.init();
        profileWriter.write(applicationId, profile);

        String actual = outputStream.toString().replace("\u0000", "");
        expected += mapper.writeValueAsString(profile) + "\n";
        assertEquals(actual, expected);
    }

    @Test
    public void appendingNewUserProfileSnapshotToMultipleExistingUserProfiles() throws Exception {
        UUID applicationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserProfile profile = new UserProfile("test-user");
        profile.setUserId(userId);
        MockFSDataOutputStream outputStream = new MockFSDataOutputStream();
        DFSClient client = mock(DFSClient.class);

        String expected = populateExistingProfiles(outputStream, profile, 5);

        when(dfs.getClient()).thenReturn(client);
        when(client.exists("/" + applicationId)).thenReturn(true);
        when(client.exists("/" + applicationId + "/" + userId)).thenReturn(true);
        when(client.append("/" + applicationId + "/" + userId, BUFFER_SIZE, null, null)).thenReturn(outputStream);

        profileWriter.init();
        profileWriter.write(applicationId, profile);

        String actual = outputStream.toString().replace("\u0000", "");
        expected += mapper.writeValueAsString(profile) + "\n";
        assertEquals(actual, expected);
    }

    private String populateExistingProfiles(
            MockFSDataOutputStream outputStream,
            UserProfile profile,
            int numProfiles
    ) throws Exception {
        UserProfile up = new UserProfile(profile.getUsername());
        up.setUserId(profile.getUserId());

        for (int i = 0; i < numProfiles; i++) {
            up.setLastUpdated(DateTime.now().minusDays(i + 1));
            outputStream.write(mapper.writeValueAsString(up));
            outputStream.write("\n");
        }

        return outputStream.toString();
    }

    private static class MockFSDataOutputStream extends FSDataOutputStream {

        private StringBuilder stream = new StringBuilder();

        public MockFSDataOutputStream() throws IOException {
            super(null, null);
        }

        @Override
        public void write(byte[] buffer, int off, int len) throws IOException {
            stream.append(new String(buffer));
        }

        public void write(String s) {
            stream.append(s);
        }

        @Override
        public void close() throws IOException {}

        @Override
        public String toString() {
            return stream.toString();
        }
    }
}
