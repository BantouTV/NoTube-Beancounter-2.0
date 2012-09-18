package io.beancounter.usermanager;

import io.beancounter.commons.model.OAuthToken;
import io.beancounter.commons.model.Service;
import io.beancounter.commons.model.User;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.auth.OAuthAuth;
import io.beancounter.commons.model.auth.SimpleAuth;
import io.beancounter.commons.tests.RandomBean;
import io.beancounter.commons.tests.Tests;
import io.beancounter.commons.tests.TestsBuilder;
import io.beancounter.commons.tests.TestsException;
import io.beancounter.commons.tests.randomisers.IntegerRandomiser;
import io.beancounter.commons.tests.randomisers.UUIDRandomiser;
import io.beancounter.usermanager.services.auth.ServiceAuthorizationManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class MockUserManager implements UserManager {

    private Tests tests = TestsBuilder.getInstance().build();

    private List<UUID> userUUIDs = new ArrayList<UUID>();

    public MockUserManager() {
        // instantiate users
        for (int i = 0; i <= (new IntegerRandomiser("anon", 20)).getRandom(); i++) {
            userUUIDs.add(new UUIDRandomiser("anon").getRandom());
        }
    }

    @Override
    public void storeUser(User user) throws UserManagerException {
    }

    @Override
    public synchronized User getUser(String username) throws UserManagerException {
        User user = null;
        if (!username.equals("missing-user")) {
            try {
                user = tests.build(User.class).getObject();
            } catch (TestsException e) {
                throw new UserManagerException("Error while building random user with username [" + username + "]");
            }
            user.setId("user-with-no-activities".equals(username)
                    ? UUID.fromString("0ad77722-1338-4c32-9209-5b952530959d")
                    : UUID.randomUUID());
            user.addService("fake-oauth-service", new OAuthAuth("fake-session", "fake-secret"));
            user.addService("fake-simple-service", new SimpleAuth("fake-session", "fake-username"));
            Collection<Service> services = new ArrayList<Service>();
            try {
                Collection<RandomBean<Service>> randomBeansServices = tests.build(Service.class, 3);
                for (RandomBean<Service> bean : randomBeansServices) {
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
                throw new UserManagerException("Error while building random services for user with username [" + username + "]");
            }
            user.setPassword("abc");
        }
        return user;
    }

    @Override
    public void deleteUser(User user) throws UserManagerException {}

    @Override
    public OAuthToken getOAuthToken(String serviceName, String username, URL url) throws UserManagerException {
        throw new UnsupportedOperationException("nah, NIY");
    }

    @Override
    public OAuthToken getOAuthToken(String service, String username)
            throws UserManagerException {
        try {
            return new OAuthToken(new URL("http://testurl.com/"));
        } catch (MalformedURLException e) {
            // it never happens
        }
        return null;
    }

    @Override
    public void registerService(String service, User user, String token)
            throws UserManagerException {}

    @Override
    public User registerOAuthService(String service, User user, String token, String verifier)
            throws UserManagerException {
        return null;
    }

    @Override
    public synchronized ServiceAuthorizationManager getServiceAuthorizationManager()
            throws UserManagerException {
        return new MockServiceAuthorizationManager();
    }

    @Override
    public void deregisterService(String service, User userObj)
            throws UserManagerException {
    }

    @Override
    public void setUserFinalRedirect(String username, URL url)
            throws UserManagerException {
    }

    @Override
    public URL consumeUserFinalRedirect(String username)
            throws UserManagerException {
        try {
            return new URL("http://testurl.com/");
        } catch (MalformedURLException e) {
            // it never happens
        }
        return null;
    }

    @Override
    public void voidOAuthToken(User user, String service) throws UserManagerException {
        throw new UnsupportedOperationException("nah, niy");
    }

    @Override
    public OAuthToken getOAuthToken(String service) throws UserManagerException {
        throw new UnsupportedOperationException("nah, niy");
    }

    @Override
    public OAuthToken getOAuthToken(String service, URL finalRedirectUrl) throws UserManagerException {
        throw new UnsupportedOperationException("nah, niy");
    }

    @Override
    public AtomicSignUp storeUserFromOAuth(String service, String token, String verifier) throws UserManagerException {
        throw new UnsupportedOperationException("nah, niy");
    }

    @Override
    public AtomicSignUp storeUserFromOAuth(String service, String token, String verifier, String decodedFinalRedirect) throws UserManagerException {
        throw new UnsupportedOperationException("nah, niy");
    }

    @Override
    public List<Activity> grabUserActivities(User user, String identifier, String service, int limit) throws UserManagerException {
        throw new UnsupportedOperationException("nah, niy");
    }

}