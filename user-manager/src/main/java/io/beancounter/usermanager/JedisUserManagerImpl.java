package io.beancounter.usermanager;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.beancounter.commons.model.auth.Auth;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import io.beancounter.commons.model.OAuthToken;
import io.beancounter.commons.model.User;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.auth.AuthHandler;
import io.beancounter.commons.model.auth.AuthHandlerException;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import io.beancounter.commons.model.auth.AuthenticatedUser;
import io.beancounter.commons.model.auth.OAuthAuth;
import io.beancounter.resolver.Resolver;
import io.beancounter.resolver.ResolverException;
import io.beancounter.resolver.ResolverMappingNotFoundException;
import io.beancounter.usermanager.services.auth.ServiceAuthorizationManager;
import io.beancounter.usermanager.services.auth.ServiceAuthorizationManagerException;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <i>REDIS</i>-based implementation of {@link UserManager}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class JedisUserManagerImpl implements UserManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(JedisUserManagerImpl.class);

    private JedisPool pool;

    private ObjectMapper mapper;

    private Map<String, URL> redirects = new HashMap<String, URL>();

    private Resolver resolver;

    private ServiceAuthorizationManager sam;

    @Inject
    @Named("redis.db.users")
    private int database;

    @Inject
    public JedisUserManagerImpl(
            JedisPoolFactory factory,
            Resolver resolver,
            ServiceAuthorizationManager sam
    ) {
        pool = factory.build();
        mapper = new ObjectMapper();
        this.resolver = resolver;
        this.sam = sam;
    }

    @Override
    public synchronized void storeUser(User user) throws UserManagerException {
        Jedis jedis;
        String userJson;
        try {
            userJson = mapper.writeValueAsString(user);
        } catch (IOException e) {
            final String errMsg = "Error while getting json for user [" + user.getId() + "]";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(
                    errMsg,
                    e
            );
        }
        jedis = pool.getResource();
        jedis.select(database);
        try {
            jedis.set(user.getUsername(), userJson);
        } finally {
            pool.returnResource(jedis);
        }
    }

    @Override
    public User getUser(String username) throws UserManagerException {
        Jedis jedis;
        String userJson;
        jedis = pool.getResource();
        jedis.select(database);
        try {
            userJson = jedis.get(username);
        } finally {
            pool.returnResource(jedis);
        }
        if (userJson == null) {
            return null;
        }
        try {
            return mapper.readValue(userJson, User.class);
        } catch (IOException e) {
            final String errMsg = "Error while getting json for user with username [" + username + "]";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(
                    errMsg,
                    e
            );
        }
    }

    @Override
    public synchronized void deleteUser(String username) throws UserManagerException {
        Jedis jedis = pool.getResource();
        jedis.select(database);
        try {
            jedis.del(username);
        } finally {
            pool.returnResource(jedis);
        }
    }

    @Override
    public OAuthToken getOAuthToken(
            String serviceName,
            String username
    ) throws UserManagerException {
        AuthHandler authHandler = getAuthHandlerForService(serviceName);

        try {
            return authHandler.getToken(username);
        } catch (AuthHandlerException e) {
            final String errMsg = "Error while getting OAuth token for service '" + serviceName + "'";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        }
    }

    @Override
    public OAuthToken getOAuthToken(String serviceName) throws UserManagerException {
        AuthHandler authHandler = getAuthHandlerForService(serviceName);

        try {
            return authHandler.getToken();
        } catch (AuthHandlerException e) {
            final String errMsg = "Error while getting OAuth token for service '" + serviceName + "'";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        }
    }

    @Override
    public OAuthToken getOAuthToken(String serviceName, URL finalRedirectUrl)
            throws UserManagerException {
        AuthHandler authHandler = getAuthHandlerForService(serviceName);

        try {
            return authHandler.getToken(finalRedirectUrl);
        } catch (AuthHandlerException e) {
            final String errMsg = "Error while getting OAuth token for service '" + serviceName + "'";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        }
    }

    @Override
    public OAuthToken getOAuthToken(String serviceName, String username, URL callback)
            throws UserManagerException {
        AuthHandler authHandler = getAuthHandlerForService(serviceName);

        try {
            return authHandler.getToken(username, callback);
        } catch (AuthHandlerException e) {
            final String errMsg = "Error while getting OAuth token for service '" + serviceName + "'";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        }
    }

    @Override
    public synchronized AtomicSignUp storeUserFromOAuth(String serviceName, String token, String verifier)
            throws UserManagerException {
        AuthHandler authHandler = getAuthHandlerForService(serviceName);

        // authorize the user
        AuthenticatedUser authUser;
        try {
            if (token == null) {
                authUser = authHandler.auth(verifier);
            } else {
                authUser = authHandler.auth(token, verifier);
            }
        } catch (AuthHandlerException e) {
            final String errMsg = "Error authorizing anonymous user for service [" + serviceName + "]";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        }

        return signUpAuthenticatedUser(authHandler.getService(), authUser);
    }

    @Override
    public AtomicSignUp storeUserFromOAuth(
            String serviceName,
            String token,
            String verifier,
            String decodedFinalRedirect
    ) throws UserManagerException {
        AuthHandler authHandler = getAuthHandlerForService(serviceName);

        // authorize the user
        AuthenticatedUser authUser;
        try {
            if (token == null) {
                authUser = authHandler.authWithRedirect(
                        verifier,
                        decodedFinalRedirect
                );
            } else {
                authUser = authHandler.auth(token, verifier);
            }
        } catch (AuthHandlerException e) {
            final String errMsg = "Error authorizing anonymous user for service [" + serviceName + "]";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        }

        return signUpAuthenticatedUser(authHandler.getService(), authUser);
    }

    @Override
    public List<Activity> grabUserActivities(
            User user,
            String identifier,
            String serviceName,
            int limit
    ) throws UserManagerException {
        AuthHandler authHandler = getAuthHandlerForService(serviceName);

        OAuthAuth auth = (OAuthAuth) user.getAuth(serviceName);
        if (auth == null) {
            final String errMsg = "it seems there is no auth for service [" + serviceName + "] on user [" + user.getUsername() + "]";
            LOGGER.error(errMsg);
            throw new UserManagerException(errMsg);
        }
        if (auth.isExpired()) {
            final String errMsg = "OAuth token for [" + serviceName + "] on user [" + user.getUsername() + "] is expired";
            LOGGER.error(errMsg);
            throw new UserManagerException(errMsg);
        }
        try {
            return authHandler.grabActivities(auth.getSession(), identifier, limit);
        } catch (AuthHandlerException e) {
            final String errMsg = "OAuth token for [" + serviceName + "] on user [" + user.getUsername() + "] is expired";
            LOGGER.error(errMsg);
            throw new UserManagerException(errMsg);
        }
    }

    @Override
    public synchronized void registerService(
            String serviceName,
            User user,
            String token
    ) throws UserManagerException {
        checkServiceIsSupported(serviceName);

        AuthenticatedUser authenticatedUser;
        try {
            authenticatedUser = sam.getHandler(serviceName).auth(
                    user,
                    token,
                    null
            );
        } catch (AuthHandlerException e) {
            final String errMsg = "Error while getting auth manager for service '" + serviceName + "'";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        } catch (ServiceAuthorizationManagerException e) {
            final String errMsg = "Error while authenticating user '" + user.getUsername() + "' to service '" + serviceName + "'";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        }
        storeUser(authenticatedUser.getUser());
    }

    @Override
    public synchronized void registerOAuthService(
            String serviceName,
            User user,
            String token,
            String verifier
    ) throws UserManagerException {
        AuthHandler authHandler = getAuthHandlerForService(serviceName);

        // now that the user grant the permission, we should ask for its username
        AuthenticatedUser auser;
        try {
            auser = authHandler.auth(
                    user,
                    token,
                    verifier
            );
        } catch (AuthHandlerException e) {
            final String errMsg = "Error while getting auth manager for service '" + serviceName + "'";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        }
        try {
            resolver.store(
                    auser.getUserId(),
                    authHandler.getService(),
                    auser.getUser().getId(),
                    auser.getUser().getUsername()
            );
        } catch (ResolverException e) {
            final String errMsg = "Error while storing username for user [" + user.getUsername() + "] on service [" + serviceName + "]";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        }
        storeUser(auser.getUser());
    }


    @Override
    public ServiceAuthorizationManager getServiceAuthorizationManager() throws UserManagerException {
        if (sam == null) {
            final String errMsg = "SAM not yet configured";
            LOGGER.error(errMsg);
            throw new UserManagerException(errMsg);
        }
        return sam;
    }

    @Override
    public synchronized void deregisterService(
            String service,
            User user) throws UserManagerException {
        user.removeService(service);
        storeUser(user);
    }

    @Override
    public void setUserFinalRedirect(String username, URL url) throws UserManagerException {
        redirects.put(username, url);
    }

    @Override
    public URL consumeUserFinalRedirect(String username) throws UserManagerException {
        if (redirects.containsKey(username)) {
            URL redirect = redirects.get(username);
            redirects.remove(username);
            return redirect;
        }
        throw new UserManagerException("It seems that a temporary url for this user has not been set yet.");
    }

    @Override
    public synchronized void voidOAuthToken(User user, String service) throws UserManagerException {
        OAuthAuth auth = (OAuthAuth) user.getAuth(service);
        if (auth == null) {
            throw new UserManagerException("it seems there is no auth for service [" + service + "] on user [" + user.getUsername() + "]");
        }
        auth.setExpired(true);
        user.removeService(service);
        user.addService(service, auth);
        this.storeUser(user);
    }

    void checkServiceIsSupported(String serviceName) throws UserManagerException {
        try {
            if (sam.getService(serviceName) == null) {
                final String errMsg = "Service '" + serviceName + "' is not supported";
                LOGGER.error(errMsg);
                throw new UserManagerException(errMsg);
            }
        } catch (ServiceAuthorizationManagerException e) {
            final String errMsg = "Error while getting service '" + serviceName + "'";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        }
    }

    AuthHandler getAuthHandlerForService(String serviceName) throws UserManagerException {
        checkServiceIsSupported(serviceName);

        AuthHandler authHandler;
        try {
            authHandler = sam.getHandler(serviceName);
        } catch (ServiceAuthorizationManagerException e) {
            final String errMsg = "Error while getting AuthHandler for service [" + serviceName + "]";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        }
        return authHandler;
    }

    private AtomicSignUp signUpAuthenticatedUser(String service, AuthenticatedUser authUser) throws UserManagerException {
        String candidateUsername;

        try {
            // Check if the user already exists
            candidateUsername = resolver.resolveUsername(
                    authUser.getUserId(),
                    service
            );
        } catch (ResolverMappingNotFoundException e) {
            // ok, this is the first access from this user so just add record
            // to the resolver and return
            User user = authUser.getUser();
            mapUserToServiceInResolver(service, authUser);
            storeUser(user);
            return new AtomicSignUp(
                    user.getId(),
                    user.getUsername(),
                    false,
                    service,
                    authUser.getUserId()
            );
        } catch (ResolverException e) {
            final String errMsg = "Error while asking mapping for user [" + authUser.getUser().getUsername() + "] with identifier [" + authUser.getUserId() + "] on service [" + service + "]";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        }

        User user = authUser.getUser();
        updateUserWithOAuthCredentials(service, user.getAuth(service), candidateUsername);

        return new AtomicSignUp(user.getId(), user.getUsername(), true, service, authUser.getUserId());
    }

    private void mapUserToServiceInResolver(
            String service,
            AuthenticatedUser authUser
    ) throws UserManagerException {
        try {
            resolver.store(
                    authUser.getUserId(),
                    service,
                    authUser.getUser().getId(),
                    authUser.getUser().getUsername()
            );
        } catch (ResolverException e) {
            final String errMsg = "Error while storing mapping for user [" + authUser.getUser().getUsername() + "] with identifier [" + authUser.getUserId() + "] on service [" + service + "]";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        }
    }

    private void updateUserWithOAuthCredentials(
            String service,
            Auth auth,
            String username
    ) throws UserManagerException {
        User user = getUser(username);
        user.addService(service, auth);
        storeUser(user);
    }
}
