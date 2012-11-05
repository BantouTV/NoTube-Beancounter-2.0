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
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    private UserTokenManager tokenManager;

    @Inject
    @Named("redis.db.users")
    private int database;

    @Inject
    public JedisUserManagerImpl(
            JedisPoolFactory factory,
            Resolver resolver,
            ServiceAuthorizationManager sam,
            UserTokenManager tokenManager) {
        pool = factory.build();
        mapper = new ObjectMapper();
        this.resolver = resolver;
        this.sam = sam;
        this.tokenManager = tokenManager;
    }

    @Override
    public synchronized void storeUser(User user) throws UserManagerException {
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
        Jedis jedis = getJedisResource();
        boolean isConnectionIssue = false;
        try {
            jedis.set(user.getUsername(), userJson);
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while storing user [" + user.getId() + "]";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        } catch (Exception e) {
            final String errMsg = "Error while storing user [" + user.getId() + "]";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        } finally {
            if (isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
            }
        }
    }

    @Override
    public User getUser(String username) throws UserManagerException {
        Jedis jedis = getJedisResource();
        String userJson;
        boolean isConnectionIssue = false;
        try {
            userJson = jedis.get(username);
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while retrieving user [" + username + "]";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        } catch (Exception e) {
            final String errMsg = "Error while retrieving user [" + username + "]";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        } finally {
            if (isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
            }
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
    public synchronized void deleteUser(User user) throws UserManagerException {
        if (user.getUserToken() != null) {
            tokenManager.deleteUserToken(user.getUserToken());
        }

        String username = user.getUsername();
        Jedis jedis = getJedisResource();
        boolean isConnectionIssue = false;
        try {
            jedis.del(username);
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while deleting user [" + username + "]";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        } catch (Exception e) {
            final String errMsg = "Error while deleting user [" + username + "]";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        } finally {
            if (isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
            }
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
    @Deprecated
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
            return authHandler.grabActivities(auth, identifier, limit);
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
    public synchronized AuthenticatedUser registerOAuthService(
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

        User userWithAuth = auser.getUser();
        try {
            resolver.store(
                    auser.getUserId(),
                    serviceName,
                    userWithAuth.getId(),
                    userWithAuth.getUsername()
            );
        } catch (ResolverException e) {
            final String errMsg = "Error while storing username for user [" + user.getUsername() + "] on service [" + serviceName + "]";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        }

        if (userWithAuth.getUserToken() != null) {
            tokenManager.deleteUserToken(userWithAuth.getUserToken());
        }
        UUID userToken = tokenManager.createUserToken(userWithAuth.getUsername());
        userWithAuth.setUserToken(userToken);
        storeUser(userWithAuth);

        return new AuthenticatedUser(auser.getUserId(), userWithAuth);
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
            return nonExistentUser(service, authUser);
        } catch (ResolverException e) {
            final String errMsg = "Error while asking mapping for user [" + authUser.getUser().getUsername() + "] with identifier [" + authUser.getUserId() + "] on service [" + service + "]";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        }
        // check if the user is really in the usermanager db
        User reallyExistentUser = getUser(candidateUsername);
        if (reallyExistentUser == null) {
            // this code is executed to be sure that if in the resolver we have
            // a mapping but there is no a user object in the usermanager db (due to some corrupted data)
            // then the sign is guaranteed to be completed
            return nonExistentUser(service, authUser);
        }
        User user = authUser.getUser();
        reallyExistentUser.setMetadata(user.getMetadata());
        User alreadyExistentUser = updateUserWithOAuthCredentials(reallyExistentUser, service, user.getAuth(service));
        UUID userToken = alreadyExistentUser.getUserToken();

        return new AtomicSignUp(alreadyExistentUser.getId(), user.getUsername(), true, service, authUser.getUserId(), userToken);
    }

    private AtomicSignUp nonExistentUser(String service, AuthenticatedUser authUser)
            throws UserManagerException {
        // ok, this is the first access from this user so:
        // 1. Create a new user token for them (no need to check if there
        //    is an existing one because this is a new user).
        // 2. Add a record to the resolver
        User user = authUser.getUser();
        UUID userToken = tokenManager.createUserToken(user.getUsername());
        user.setUserToken(userToken);
        mapUserToServiceInResolver(service, authUser);
        storeUser(user);
        return new AtomicSignUp(
                user.getId(),
                user.getUsername(),
                false,
                service,
                authUser.getUserId(),
                userToken
        );
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

    private User updateUserWithOAuthCredentials(
            User user,
            String service,
            Auth auth
    ) throws UserManagerException {
        user.addService(service, auth);
        if (user.getUserToken() != null) {
            tokenManager.deleteUserToken(user.getUserToken());
        }
        UUID userToken = tokenManager.createUserToken(user.getUsername());
        user.setUserToken(userToken);
        storeUser(user);
        return user;
    }

    private Jedis getJedisResource() throws UserManagerException {
        Jedis jedis;
        try {
            jedis = pool.getResource();
        } catch (Exception e) {
            final String errMsg = "Error while getting a Jedis resource";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        }
        boolean isConnectionIssue = false;
        try {
            jedis.select(database);
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while selecting database [" + database + "]";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        } catch (Exception e) {
            pool.returnResource(jedis);
            final String errMsg = "Error while selecting database [" + database + "]";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        } finally {
            if (isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            }
        }
        return jedis;
    }
}
