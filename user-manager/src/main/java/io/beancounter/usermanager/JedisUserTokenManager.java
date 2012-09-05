package io.beancounter.usermanager;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.UUID;

/**
 * <i>REDIS</i>-based implementation of {@link UserTokenManager}.
 */
public class JedisUserTokenManager implements UserTokenManager {

    private JedisPool jedisPool;

    private int database;

    @Inject
    public JedisUserTokenManager(JedisPoolFactory jedisPoolFactory) {
        jedisPool = jedisPoolFactory.build();
    }

    @Override
    public boolean checkTokenExists(UUID token) throws UserManagerException {
        if (token == null) {
            throw new UserManagerException("User token cannot be null");
        }

        Jedis jedis = null;
        boolean isConnectionIssue = false;

        try {
            jedis = jedisPool.getResource();
            jedis.select(database);
            return jedis.exists(token.toString());
        } catch (JedisConnectionException jce) {
            isConnectionIssue = true;
            throw new UserManagerException(jce.getMessage(), jce);
        } catch (Exception ex) {
            throw new UserManagerException(ex.getMessage(), ex);
        } finally {
            if (jedis != null) {
                if (isConnectionIssue) {
                    jedisPool.returnBrokenResource(jedis);
                } else {
                    jedisPool.returnResource(jedis);
               }
            }
        }
    }

    @Override
    public UUID createUserToken(String username) throws UserManagerException {
        if (username == null) {
            throw new UserManagerException("Username for user token cannot be null");
        }

        Jedis jedis = null;
        boolean isConnectionIssue = false;

        try {
            jedis = jedisPool.getResource();
            jedis.select(database);
            UUID userToken = UUID.randomUUID();
            jedis.set(userToken.toString(), username);
            return userToken;
        } catch (JedisConnectionException jce) {
            isConnectionIssue = true;
            throw new UserManagerException(jce.getMessage(), jce);
        } catch (Exception ex) {
            throw new UserManagerException(ex.getMessage(), ex);
        } finally {
            if (jedis != null) {
                if (isConnectionIssue) {
                    jedisPool.returnBrokenResource(jedis);
                } else {
                    jedisPool.returnResource(jedis);
                }
            }
        }
    }

    @Override
    public boolean deleteUserToken(UUID token) throws UserManagerException {
        if (token == null) {
            throw new UserManagerException("User token cannot be null");
        }

        Jedis jedis = null;
        boolean isConnectionIssue = false;

        try {
            jedis = jedisPool.getResource();
            jedis.select(database);
            return jedis.del(token.toString()) > 0;
        } catch (JedisConnectionException jce) {
            isConnectionIssue = true;
            throw new UserManagerException(jce.getMessage(), jce);
        } catch (Exception ex) {
            throw new UserManagerException(ex.getMessage(), ex);
        } finally {
            if (jedis != null) {
                if (isConnectionIssue) {
                    jedisPool.returnBrokenResource(jedis);
                } else {
                    jedisPool.returnResource(jedis);
                }
            }
        }
    }

    /**
     * Set the number of the database in Redis that is used for storing user
     * tokens.
     *
     * @param database The Redis database number.
     */
    @Inject
    public void setDatabase(@Named("redis.db.userTokens") int database) {
        if (database < 0) {
            throw new IllegalArgumentException("Database number must be at least 0");
        }

        this.database = database;
    }
}
