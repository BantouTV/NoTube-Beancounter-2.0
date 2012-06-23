package tv.notube.usermanager;

import com.google.inject.Inject;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import tv.notube.commons.model.OAuthToken;
import tv.notube.commons.model.User;
import tv.notube.commons.model.auth.AuthHandler;
import tv.notube.commons.model.auth.AuthHandlerException;
import tv.notube.usermanager.jedis.JedisPoolFactory;
import tv.notube.usermanager.services.auth.ServiceAuthorizationManager;
import tv.notube.usermanager.services.auth.ServiceAuthorizationManagerException;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class JedisUserManagerImpl implements UserManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(JedisUserManagerImpl.class);

    private JedisPool pool;

    private ObjectMapper mapper;

    private Map<String, URL> redirects = new HashMap<String, URL>();

    private ServiceAuthorizationManager sam;

    @Inject
    public JedisUserManagerImpl(
            JedisPoolFactory factory,
            ServiceAuthorizationManager sam
    ) {
        pool = factory.build();
        mapper = new ObjectMapper();
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
        try {
            if (sam.getService(serviceName) == null) {
                final String errMsg = "Service '" + serviceName + "' is not supported.";
                LOGGER.error(errMsg);
                throw new UserManagerException(errMsg);
            }
        } catch (ServiceAuthorizationManagerException e) {
            final String errMsg = "Error while getting service '" + serviceName + "'";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        }
        AuthHandler authHandler;
        try {
            authHandler = sam.getHandler(serviceName);
        } catch (ServiceAuthorizationManagerException e) {
            final String errMsg = "Error while getting auth manager for service '" + serviceName + "'";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        }
        try {
            return authHandler.getToken(username);
        } catch (AuthHandlerException e) {
            final String errMsg = "Error while getting auth manager for service '" + serviceName + "'";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        }
    }

    @Override
    public synchronized void registerService(
            String serviceName,
            User user,
            String token
    ) throws UserManagerException {
        try {
            if (sam.getService(serviceName) == null) {
                final String errMsg = "Service '" + serviceName + "' is not supported.";
                LOGGER.error(errMsg);
                throw new UserManagerException(errMsg);
            }
        } catch (ServiceAuthorizationManagerException e) {
            final String errMsg = "Error while getting service '" + serviceName + "'";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        }
        User authenticatedUser;
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
        storeUser(authenticatedUser);
    }

    @Override
    public synchronized void registerOAuthService(
            String serviceName,
            User user,
            String token,
            String verifier
    ) throws UserManagerException {
        try {
            if (sam.getService(serviceName) == null) {
                final String errMsg = "Service '" + serviceName + "' is not supported.";
                LOGGER.error(errMsg);
                throw new UserManagerException(errMsg);
            }
        } catch (ServiceAuthorizationManagerException e) {
            final String errMsg = "Error while getting service '" + serviceName + "'";
            LOGGER.error(errMsg, e);
            throw new UserManagerException(errMsg, e);
        }
        User authenticatedUser;
        try {
            authenticatedUser = sam.getHandler(serviceName).auth(
                    user,
                    token,
                    verifier
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
        storeUser(authenticatedUser);
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
}
