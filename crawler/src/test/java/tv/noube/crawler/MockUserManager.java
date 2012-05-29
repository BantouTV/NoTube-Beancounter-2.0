package tv.noube.crawler;

import tv.notube.commons.model.OAuthToken;
import tv.notube.commons.model.User;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.tests.Tests;
import tv.notube.commons.tests.TestsBuilder;
import tv.notube.commons.tests.TestsException;
import tv.notube.usermanager.UserManager;
import tv.notube.usermanager.UserManagerException;
import tv.notube.usermanager.services.auth.ServiceAuthorizationManager;

import java.net.URL;
import java.util.List;
import java.util.UUID;

public class MockUserManager implements UserManager {

    private Tests tests = TestsBuilder.build();

    @Override
    public void storeUser(User user) throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public User getUser(UUID userId) throws UserManagerException {
        User user;
        try {
            user = tests.build(User.class).getObject();
        } catch (TestsException e) {
            throw new UserManagerException("Error while building random user with id [" + userId + "]");
        }
        user.setId(userId);
        return user;
    }

    @Override
    public User getUser(String username) throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void storeUserActivities(UUID userId, List<Activity> activities) throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public List<Activity> getUserActivities(UUID userId) throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public List<Activity> getUserActivities(String username) throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void deleteUser(UUID userId) throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public List<UUID> getUsersToBeProfiled() throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public List<UUID> getUsersToCrawled() throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public OAuthToken getOAuthToken(String service, String username) throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void registerService(String service, User user, String token) throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void registerOAuthService(String service, User user, String token, String verifier) throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public ServiceAuthorizationManager getServiceAuthorizationManager() throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void deregisterService(String service, User userObj) throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void setUserFinalRedirect(String username, URL url) throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public URL consumeUserFinalRedirect(String username) throws UserManagerException {
        throw new UnsupportedOperationException("NIY");
    }
}