package tv.notube.resolver;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import tv.notube.commons.helper.jedis.JedisPoolFactory;
import tv.notube.commons.helper.resolver.Services;
import tv.notube.commons.model.activity.Activity;

import java.util.UUID;

/**
 * <i>Redis</i>-based implementation of {@link Resolver}.
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
@Singleton
public class JedisResolver implements Resolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(JedisResolver.class);

    private JedisPool pool;

    private Services services;

    @Inject
    public JedisResolver(
            JedisPoolFactory factory,
            Services services
    ) {
        pool = factory.build();
        this.services = services;
    }

    @Override
    public UUID resolve(Activity activity) {
        String service = activity.getContext().getService();
        int database;
        try {
            database = services.get(service);
        } catch (NullPointerException e) {
            LOGGER.error("Service [{}] not supported", service, e);
            return null;
        }
        Jedis jedis = pool.getResource();
        jedis.select(database);
        String userIdentifier = activity.getContext().getUsername();
        String uvStr;
        try {
            uvStr = jedis.get(userIdentifier);
        } finally {
            pool.returnResource(jedis);
        }
        if(uvStr == null) {
            LOGGER.error("User [{}] not found for [{}]", userIdentifier, service);
            return null;
        }
        UserValues uvObj = UserValues.parse(uvStr);
        UUID userId = uvObj.getUserId();
        if (userId == null) {
            LOGGER.error("User [{}] not found for [{}]", userIdentifier, service);
            return null;
        }
        return userId;
    }

    @Override
    public UUID resolveId(String identifier, String service) throws ResolverException {
        int database;
        try {
            database = services.get(service);
        } catch (NullPointerException e) {
            LOGGER.error("Service [{}] not supported", service, e);
            return null;
        }
        Jedis jedis = pool.getResource();
        jedis.select(database);
        String uvStr;
        try {
            uvStr = jedis.get(identifier);
        } finally {
            pool.returnResource(jedis);
        }
        UserValues uv = UserValues.parse(uvStr);
        UUID userId = uv.getUserId();
        if (userId == null) {
            LOGGER.error("User [{}] not found for [{}]", identifier, service);
            return null;
        }
        return userId;
    }

    @Override
    public String resolveUsername(String identifier, String service) throws ResolverException {
        int database;
        try {
            database = services.get(service);
        } catch (NullPointerException e) {
            LOGGER.error("Service [{}] not supported", service, e);
            return null;
        }
        Jedis jedis = pool.getResource();
        jedis.select(database);
        String uvStr;
        try {
            uvStr = jedis.get(identifier);
        } finally {
            pool.returnResource(jedis);
        }
        UserValues uv = UserValues.parse(uvStr);
        String username = uv.getUsername();
        if (username == null) {
            LOGGER.error("User [{}] not found for [{}]", identifier, service);
            return null;
        }
        return username;
    }

    @Override
    public void store(String identifier, String service, UUID userId, String username)
            throws ResolverException {
        if (username == null) {
            throw new IllegalArgumentException("username parameter cannot be null");
        }
        if (service == null) {
            throw new IllegalArgumentException("service parameter cannot be null");
        }
        int database;
        try {
            database = services.get(service);
        } catch (NullPointerException e) {
            final String errMsg = "Service [" + service + "] not supported";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        }
        Jedis jedis = pool.getResource();
        jedis.select(database);
        UserValues uv = new UserValues(userId, username);
        try {
            jedis.set(identifier, UserValues.unparse(uv));
        } finally {
            pool.returnResource(jedis);
        }
    }


}