package io.beancounter.profiles;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import io.beancounter.commons.model.UserProfile;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;

import java.io.IOException;
import java.util.UUID;

/**
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class JedisProfilesImpl implements Profiles {

    private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(JedisProfilesImpl.class);

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
        Jedis jedis;
        try {
            jedis = pool.getResource();
        } catch (Exception e) {
            final String errMsg = "Error while getting a Jedis resource";
            LOGGER.error(errMsg, e);
            throw new ProfilesException(errMsg, e);
        }
        try {
            jedis.select(database);
        } catch (Exception e) {
            pool.returnResource(jedis);
            final String errMsg = "Error while selecting database [" + database + "] for user profile [" + up.getUserId() + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilesException(errMsg, e);
        }
        LOGGER.debug("storing profile for user [" + up.getUserId() + "] on database [" + database + "]");
        try {
            jedis.set(up.getUserId().toString(), userProfile);
        } catch (Exception e) {
            final String errMsg = "Error while storing profile for user [" + up.getUserId() + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilesException(errMsg, e);
        } finally {
            pool.returnResource(jedis);
        }
        LOGGER.debug("profile for user [" + up.getUserId() + "] stored");
    }

    @Override
    public UserProfile lookup(UUID userId) throws ProfilesException {
        LOGGER.debug("looking up profile for user [" + userId + "] stored");
        UserProfile profile;
        Jedis jedis;
        try {
            jedis = pool.getResource();
        } catch (Exception e) {
            final String errMsg = "Error while getting a Jedis resource";
            LOGGER.error(errMsg, e);
            throw new ProfilesException(errMsg, e);
        }
        try {
            jedis.select(database);
        } catch (Exception e) {
            pool.returnResource(jedis);
            final String errMsg = "Error while selecting database [" + database + "] for user profile [" + userId + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilesException(errMsg, e);
        }
        String stringProfile;
        try {
            stringProfile = jedis.get(userId.toString());
        } catch (Exception e) {
            final String errMsg = "Error while retrieving user profile for user [" + userId + "]";
            LOGGER.error(errMsg, e);
            throw new ProfilesException(errMsg, e);
        } finally {
            pool.returnResource(jedis);
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
}