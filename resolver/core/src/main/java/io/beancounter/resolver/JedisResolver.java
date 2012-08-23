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
import redis.clients.jedis.exceptions.JedisConnectionException;

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
        Jedis jedis = getJedisResource(database);
        String userIdentifier = activity.getContext().getUsername();
        String userId;
        boolean isConnectionIssue = false;
        try {
            userId = jedis.hget(userIdentifier, "uuid");
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while getting userId for [" + userIdentifier
                    + "] for service [" + service + "]";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        } catch (Exception e) {
            final String errMsg = "Error while getting userId for [" + userIdentifier + "] for service [" + service + "]";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        } finally {
            if(isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
            }
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
        Jedis jedis = getJedisResource(database);
        String userId;
        boolean isConnectionIssue = false;
        try {
            userId = jedis.hget(identifier, "uuid");
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while getting userId for [" + identifier + "] for service ["
                    + service + "]";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        } catch (Exception e) {
            final String errMsg = "Error while getting userId for [" + identifier + "] for service [" + service + "]";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        } finally {
            if(isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
            }
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
        Jedis jedis = getJedisResource(database);
        String username;
        boolean isConnectionIssue = false;
        try {
            username = jedis.hget(identifier, "username");
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connectino error while getting username for [" + identifier + "] for service ["
                    + service + "]";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        } catch (Exception e) {
            final String errMsg = "Error while getting username for [" + identifier + "] for service [" + service + "]";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        } finally {
            if(isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
            }
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
        Jedis jedis = getJedisResource(database);
        boolean isConnectionIssue = false;
        try {
            jedis.hset(identifier, "uuid", userId.toString());
            jedis.hset(identifier, "username", username);
            long numberOfElements = jedis.rpush(service, userId.toString());
            jedis.hset(identifier, "index", "" + (--numberOfElements));
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while storing user [" + username + userId + "] for service ["
                    + service + "]";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        } catch (Exception e) {
            final String errMsg = "Error while storing user [" + username + userId + "] for service [" + service + "]";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        } finally {
            if(isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
            }
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
        Jedis jedis = getJedisResource(database);
        List<String> userIds;
        boolean isConnectionIssue = false;
        try {
            userIds = jedis.lrange(serviceName, start, stop);
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while retrieving userIds [from " + start + " to " + stop
                    + "] for service [" + serviceName + "]";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        } catch (Exception e) {
            final String errMsg = "Error while retrieving userIds [from " + start + " to " + stop + "] for service ["
                    + serviceName + "]";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        } finally {
            if(isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
            }
        }
        return userIds;
    }

    private Jedis getJedisResource(int database) throws ResolverException {
        Jedis jedis;
        try {
            jedis = pool.getResource();
        } catch (Exception e) {
            final String errMsg = "Error while getting a Jedis resource";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        }
        boolean isConnectionIssue = false;
        try {
            jedis.select(database);
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while selecting database [" + database + "]";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        } catch (Exception e) {
            pool.returnResource(jedis);
            final String errMsg = "Error while selecting database [" + database + "]";
            LOGGER.error(errMsg, e);
            throw new ResolverException(errMsg, e);
        } finally {
            if(isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            }
        }
        return jedis;
    }

}