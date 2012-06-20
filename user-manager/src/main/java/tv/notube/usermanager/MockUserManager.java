package tv.notube.usermanager;

import org.joda.time.DateTime;
import tv.notube.commons.model.OAuthToken;
import tv.notube.commons.model.Service;
import tv.notube.commons.model.User;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.auth.OAuthAuth;
import tv.notube.commons.model.auth.SimpleAuth;
import tv.notube.commons.tests.RandomBean;
import tv.notube.commons.tests.Tests;
import tv.notube.commons.tests.TestsBuilder;
import tv.notube.commons.tests.TestsException;
import tv.notube.commons.tests.randomisers.IntegerRandomiser;
import tv.notube.commons.tests.randomisers.UUIDRandomiser;
import tv.notube.usermanager.services.auth.ServiceAuthorizationManager;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class MockUserManager implements UserManager {

    private Tests tests = TestsBuilder.getInstance().build();

    private Map<UUID, List<Activity>> activities = new HashMap<UUID, List<Activity>>();

    private List<UUID> userUUIDs = new ArrayList<UUID>();

    public MockUserManager() {
        // instantiate users
        for(int i=0; i <= (new IntegerRandomiser("anon", 20)).getRandom(); i++) {
            userUUIDs.add(new UUIDRandomiser("anon").getRandom());
        }
    }

    @Override
    public void storeUser(User user) throws UserManagerException {}

    @Override
    public synchronized User getUser(UUID userId) throws UserManagerException {
        User user;
        try {
            user = tests.build(User.class).getObject();
        } catch (TestsException e) {
            throw new UserManagerException("Error while building random user with id [" + userId + "]");
        }
        user.setId(userId);
        user.addService("fake-oauth-service", new OAuthAuth("fake-session", "fake-secret"));
        user.addService("fake-simple-service", new SimpleAuth("fake-session", "fake-username"));
        Collection<Service> services = new ArrayList<Service>();
        try {
            Collection<RandomBean<Service>> randomBeansServices = tests.build(Service.class, 3);
            for(RandomBean<Service> bean : randomBeansServices) {
                services.add(bean.getObject());
                user.addService(
                        bean.getObject().getName(),
                        new SimpleAuth(
                                bean.getObject().getDescription(),
                                bean.getObject().getSecret()
                        )
                );
            }
        } catch (TestsException e) {
            throw new UserManagerException("Error while building random services for user with id [" + userId + "]");
        }
        return user;
    }

    @Override
    public User getUser(String username) throws UserManagerException {
        return username.equals("test-user") ? getTestUser("test-user") : null;
    }

    private User getTestUser(String username) {
        User user = new User();
        user.setId(userUUIDs.get(0));
        user.setUsername(username);
        user.setForcedProfiling(false);
        user.setName("Fake Name");
        user.setSurname("Fake Surname");
        user.setPassword("abc");
        user.setProfiledAt(DateTime.now());
        user.addService("fake-service-1", new SimpleAuth("fake-session", "fake-username"));
        user.addService("fake-service-2", new OAuthAuth("fake-session", "fake-secret"));
        return user;
    }

    @Override
    public synchronized void storeUserActivities(UUID userId, List<Activity> activities)
            throws UserManagerException {
        this.activities.put(userId, activities);
    }

    @Override
    public synchronized List<Activity> getUserActivities(UUID userId)
            throws UserManagerException {
        return activities.get(userId);
    }

    @Override
    public List<Activity> getUserActivities(String username)
            throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void deleteUser(UUID userId) throws UserManagerException {}

    @Override
    public List<UUID> getUsersToBeProfiled() throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public List<UUID> getUsersToCrawled() throws UserManagerException {
        return userUUIDs;
    }

    @Override
    public OAuthToken getOAuthToken(String service, String username)
            throws UserManagerException {
        try {
            return new OAuthToken(new URL("http://testurl.com/"));
        } catch (MalformedURLException e) {
            // it never happens
        } return null;
    }

    @Override
    public void registerService(String service, User user, String token)
            throws UserManagerException {}

    @Override
    public void registerOAuthService(String service, User user, String token, String verifier)
            throws UserManagerException {}

    @Override
    public synchronized ServiceAuthorizationManager getServiceAuthorizationManager()
            throws UserManagerException {
        return new MockServiceAuthorizationManager();
    }

    @Override
    public void deregisterService(String service, User userObj)
            throws UserManagerException {}

    @Override
    public void setUserFinalRedirect(String username, URL url)
            throws UserManagerException {}

    @Override
    public URL consumeUserFinalRedirect(String username)
            throws UserManagerException {
        try {
            return new URL("http://testurl.com/");
        } catch (MalformedURLException e) {
            // it never happens
        } return null;
    }
}