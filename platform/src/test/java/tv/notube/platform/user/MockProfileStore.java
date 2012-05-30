package tv.notube.platform.user;

import tv.notube.commons.model.Interest;
import tv.notube.commons.model.UserProfile;
import tv.notube.profiler.storage.ProfileStore;
import tv.notube.profiler.storage.ProfileStoreException;

import java.io.OutputStream;
import java.util.*;

/**
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class MockProfileStore implements ProfileStore {

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
        MockUserManager um = new MockUserManager();
        UserProfile up = new UserProfile();
        up.setVisibility(UserProfile.Visibility.PUBLIC);
        up.setUsername(username);
        Interest i1 = new Interest();
        i1.setWeight(0.5);
        i1.setActivities(um.getActivities());
        Set<Interest> interests = new HashSet<Interest>();
        interests.add(i1);
        up.setInterests(interests);
        return up;
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