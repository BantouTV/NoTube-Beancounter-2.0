package tv.notube.profiler.storage;

import tv.notube.commons.model.UserProfile;
import tv.notube.commons.tests.Tests;
import tv.notube.commons.tests.TestsBuilder;
import tv.notube.commons.tests.TestsException;

import java.io.OutputStream;
import java.util.*;

/**
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class MockProfileStore implements ProfileStore {

    private Tests tests = TestsBuilder.getInstance().build();

    @Override
    public void storeUserProfile(UserProfile userProfile) throws ProfileStoreException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public UserProfile getUserProfile(String username) throws ProfileStoreException {
        if(username.equals("test-user"))
            return getProfile(username);
        else throw new ProfileStoreException("MOCK-ERROR");
    }

    private UserProfile getProfile(String username) {
        try {
            return tests.build(UserProfile.class).getObject();
        } catch (TestsException e) {
            throw new RuntimeException("cannot build a random UserProfile for [" + username + "]", e);
        }
    }

    @Override
    public UserProfile getUserProfile(UUID userId) throws ProfileStoreException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void deleteUserProfile(String username) throws ProfileStoreException {}

    @Override
    public void deleteUserProfile(UUID userId) throws ProfileStoreException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void export(UUID userid, OutputStream outputStream, Format format) throws ProfileStoreException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void setNamespaces(Map<String, String> nameSpacesConfiguration) {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public Map<String, String> getNamespaces() {
        throw new UnsupportedOperationException("NIY");
    }
}