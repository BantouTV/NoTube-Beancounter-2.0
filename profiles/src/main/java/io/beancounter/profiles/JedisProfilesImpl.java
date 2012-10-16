package io.beancounter.profiles;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import io.beancounter.commons.model.UserProfile;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.IOException;
import java.util.UUID;

/**
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class JedisProfilesImpl implements Profiles {

    private static Logger LOGGER = LoggerFactory.getLogger(JedisProfilesImpl.class);

    private JedisPool pool;

    private ObjectMapper mapper;

    @Inject
    @Named("redis.db.profiles") private int database;

    @Inject
    public JedisProfilesImpl(JedisPoolFactory factory) {
        pool = factory.build();
        mapper = new ObjectMapper();
    }

    @Override
    public void store(UserProfile up) throws ProfilesException {
        LOGGER.debug("storing profile for user [" + up.getUserId() + "]");
        String userProfile;
        try {
            userProfile = mapper.writeValueAsString(up);
        } catch (IOException e) {
            final String errMsg = "Error while getting json for user profile [" + up.getUserId() + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilesException(
                    errMsg,
                    e
            );
        } catch (Exception e) {
            final String errMsg = "Error while getting json for user profile [" + up.getUserId() + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilesException(
                    errMsg,
                    e
            );
        }
        Jedis jedis = getJedisResource();
        LOGGER.debug("storing profile for user [" + up.getUserId() + "] on database [" + database + "]");
        boolean isConnectionIssue = false;
        try {
            jedis.set(up.getUserId().toString(), userProfile);
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while storing profile for user [" + up.getUserId() + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilesException(errMsg, e);
        } catch (Exception e) {
            final String errMsg = "Error while storing profile for user [" + up.getUserId() + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilesException(errMsg, e);
        } finally {
            if(isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
            }
        }
        LOGGER.debug("profile for user [" + up.getUserId() + "] stored");
    }

    @Override
    public UserProfile lookup(UUID userId) throws ProfilesException {
        LOGGER.debug("looking up profile for user [" + userId + "]");
        UserProfile profile;
        Jedis jedis = getJedisResource();
        String stringProfile;
        boolean isConnectionIssue = false;
        try {
            stringProfile = jedis.get(userId.toString());
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Error while retrieving user profile for user [" + userId + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilesException(errMsg, e);
        } catch (Exception e) {
            final String errMsg = "Error while retrieving user profile for user [" + userId + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilesException(errMsg, e);
        } finally {
            if(isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
            }
        }
        if(stringProfile == null) {
            return null;
        }
        try {
            profile = mapper.readValue(stringProfile, UserProfile.class);
        } catch (IOException e) {
            final String errMsg = "Error while getting json for user [" + userId + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilesException(
                    errMsg,
                    e
            );
        }
        LOGGER.debug("profile for user [" + userId + "] looked up properly");
        return profile;
    }

    private Jedis getJedisResource() throws ProfilesException {
        Jedis jedis;
        try {
            jedis = pool.getResource();
        } catch (Exception e) {
            final String errMsg = "Error while getting a Jedis resource";
            LOGGER.error(errMsg, e);
            throw new ProfilesException(errMsg, e);
        }
        boolean isConnectionIssue = false;
        try {
            jedis.select(database);
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while selecting database [" + database + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilesException(errMsg, e);
        } catch (Exception e) {
            pool.returnResource(jedis);
            final String errMsg = "Error while selecting database [" + database + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilesException(errMsg, e);
        } finally {
            if(isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            }
        }
        return jedis;
    }
}