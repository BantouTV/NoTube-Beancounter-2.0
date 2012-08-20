package io.beancounter.resolver;

import java.util.List;
import java.util.UUID;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import io.beancounter.commons.helper.resolver.Services;
import io.beancounter.commons.model.activity.Activity;

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
    public UUID resolve(Activity activity) throws ResolverException {
        String service = activity.getContext().getService();
        int database;
        try {
            database = services.get(service);
        } catch (NullPointerException e) {
            final String errmsg = "Service [" + service + "] not supported";
            LOGGER.error(errmsg, e);
            throw new ResolverException(errmsg, e);
        }
        Jedis jedis;
        try {
            jedis = pool.getResource();
        } catch (Exception e) {
            final String errMsg = "Error while getting a Jedis resource";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        }
        try {
            jedis.select(database);
        } catch (Exception e) {
            pool.returnResource(jedis);
            final String errMsg = "Error while selecting database [" + database + "] for service [" + service + "]";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        }
        String userIdentifier = activity.getContext().getUsername();
        String userId;
        try {
            userId = jedis.hget(userIdentifier, "uuid");
        } catch (Exception e) {
            final String errMsg = "Error while getting userId for [" + userIdentifier + "] for service [" + service + "]";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        } finally {
            pool.returnResource(jedis);
        }
        if (userId == null) {
            final String errmsg = "User [" + userIdentifier + "] not found for [" + service + "]";
            LOGGER.error(errmsg);
            throw new ResolverException(errmsg);
        }
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            final String errmsg = "Illformed beancounter userId [" + userId +
                    "] for userIdentifier [" + userIdentifier + "] in service [" + service + "]";
            LOGGER.error(errmsg, e);
            throw new ResolverException(errmsg, e);
        }
    }

    @Override
    public UUID resolveId(String identifier, String service) throws ResolverException {
        int database;
        try {
            database = services.get(service);
        } catch (NullPointerException e) {
            final String errmsg = "Service [" + service + "] not supported";
            LOGGER.error(errmsg, e);
            throw new ResolverException(errmsg, e);
        }
        Jedis jedis;
        try {
            jedis = pool.getResource();
        } catch (Exception e) {
            final String errMsg = "Error while getting a Jedis resource";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        }
        try {
            jedis.select(database);
        } catch (Exception e) {
            pool.returnResource(jedis);
            final String errMsg = "Error while selecting database [" + database + "] for service [" + service + "]";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        }
        String userId;
        try {
            userId = jedis.hget(identifier, "uuid");
        } catch (Exception e) {
            final String errMsg = "Error while getting userId for [" + identifier + "] for service [" + service + "]";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        } finally {
            pool.returnResource(jedis);
        }
        if (userId == null) {
            final String errmsg = "User [" + identifier + "] not found for [" + service + "]";
            LOGGER.error(errmsg);
            throw new ResolverException(errmsg);
        }
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            final String errmsg = "Illformed beancounter userId [" + userId +
                    "] for userIdentifier [" + identifier + "] in service [" + service + "]";
            LOGGER.error(errmsg, e);
            throw new ResolverException(errmsg, e);
        }
    }

    @Override
    public String resolveUsername(String identifier, String service) throws ResolverException {
        int database;
        try {
            database = services.get(service);
        } catch (NullPointerException e) {
            final String errmsg = "Service [" + service + "] not supported";
            LOGGER.error(errmsg, e);
            throw new ResolverException(errmsg, e);
        }
        Jedis jedis;
        try {
            jedis = pool.getResource();
        } catch (Exception e) {
            final String errMsg = "Error while getting a Jedis resource";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        }
        try {
            jedis.select(database);
        } catch (Exception e) {
            pool.returnResource(jedis);
            final String errMsg = "Error while selecting database [" + database + "] for service [" + service + "]";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        }
        String username;
        try {
            username = jedis.hget(identifier, "username");
        } catch (Exception e) {
            final String errMsg = "Error while getting username for [" + identifier + "] for service [" + service + "]";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        } finally {
            pool.returnResource(jedis);
        }
        if (username == null) {
            final String errmsg = "User [" + identifier + "] not found for [" + service + "]";
            LOGGER.error(errmsg);
            throw new ResolverMappingNotFoundException(errmsg);
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
        Jedis jedis;
        try {
            jedis = pool.getResource();
        } catch (Exception e) {
            final String errMsg = "Error while getting a Jedis resource";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        }
        try {
            jedis.select(database);
        } catch (Exception e) {
            pool.returnResource(jedis);
            final String errMsg = "Error while selecting database [" + database + "] for service [" + service + "]";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        }
        try {
            jedis.hset(identifier, "uuid", userId.toString());
            jedis.hset(identifier, "username", username);
            long numberOfElements = jedis.rpush(service, userId.toString());
            jedis.hset(identifier, "index", "" + (--numberOfElements));
        } catch (Exception e) {
            final String errMsg = "Error while storing user [" + username + userId + "] for service [" + service + "]";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        } finally {
            pool.returnResource(jedis);
        }
    }

    @Override
    public List<String> getUserIdsFor(String serviceName, int start, int stop) throws ResolverException {
        int database;
        try {
            database = services.get(serviceName);
        } catch (NullPointerException e) {
            final String errMsg = "Service [" + serviceName + "] not supported";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        }
        Jedis jedis;
        try {
            jedis = pool.getResource();
        } catch (Exception e) {
            final String errMsg = "Error while getting a Jedis resource";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        }
        try {
            jedis.select(database);
        } catch (Exception e) {
            pool.returnResource(jedis);
            final String errMsg = "Error while selecting database [" + database + "] for service [" + serviceName + "]";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        }
        List<String> userIds;
        try {
            userIds = jedis.lrange(serviceName, start, stop);
        } catch (Exception e) {
            final String errMsg = "Error while retrieving userIds [from " + start + " to " + stop + "] for service ["
                    + serviceName + "]";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        } finally {
            pool.returnResource(jedis);
        }
        return userIds;
    }

}