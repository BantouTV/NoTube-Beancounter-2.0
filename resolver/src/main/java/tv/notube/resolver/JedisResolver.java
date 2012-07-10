package tv.notube.resolver;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import tv.notube.commons.helper.jedis.JedisPoolFactory;
import tv.notube.commons.model.activity.Activity;

import java.util.Properties;
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
        String service = activity.getContext().getService().toString();
        int database;
        try {
            database = services.get(service);
        } catch (NullPointerException e) {
            LOGGER.error("Service [{}] not supported", service, e);
            return null;
        }
        Jedis jedis = pool.getResource();
        jedis.select(database);
        String serviceUsername = activity.getContext().getUsername();
        String userIdStr;
        try {
            userIdStr = jedis.get(serviceUsername);
        } finally {
            pool.returnResource(jedis);
        }
        UUID userId;
        try {
            userId = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            LOGGER.error(
                    "User [{}] has a wrong userId in the database",
                    serviceUsername
            );
            return null;
        }
        if (userId == null) {
            LOGGER.error("User [{}] not found for [{}]", serviceUsername, service);
            return null;
        }
        return userId;
    }

    @Override
    public void store(String username, String service, UUID userId) throws ResolverException {
        if(username == null) {
            throw new IllegalArgumentException("username parameter cannot be null");
        }
        if(service == null) {
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
        try {
            jedis.set(username, userId.toString());
        } finally {
            pool.returnResource(jedis);
        }
    }

    public static Services build(Properties properties) {
        // TODO (med) put a better properties here
        Services services = new Services();
        services.put("http://twitter.com", Integer.parseInt((String) properties.get("redis.db.twitter"), 10));
        services.put("http://facebook.com", Integer.parseInt((String) properties.get("redis.db.facebook"), 10));
        return services;
    }

}