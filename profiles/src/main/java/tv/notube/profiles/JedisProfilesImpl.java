package tv.notube.profiles;

import com.google.inject.Inject;
import org.codehaus.jackson.map.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import tv.notube.commons.model.UserProfile;
import tv.notube.commons.helper.jedis.JedisPoolFactory;

import java.io.IOException;
import java.util.UUID;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class JedisProfilesImpl implements Profiles {

    private JedisPool pool;

    private ObjectMapper mapper;

    @Inject
    public JedisProfilesImpl(JedisPoolFactory factory) {
        pool = factory.build();
        mapper = new ObjectMapper();
    }

    @Override
    public void store(UserProfile up) throws ProfilesException {
        Jedis jedis;
        String userProfile;
        try {
            userProfile = mapper.writeValueAsString(up);
        } catch (IOException e) {
            throw new ProfilesException(
                    "Error while getting json for user profile [" + up.getUserId() + "]",
                    e
            );
        }
        jedis = pool.getResource();
        try {
            jedis.set(up.getUserId().toString(), userProfile);
        } finally {
            pool.returnResource(jedis);
        }
    }

    @Override
    public UserProfile lookup(UUID userId) throws ProfilesException {
        UserProfile profile;
        Jedis jedis = pool.getResource();
        String stringProfile = jedis.get(userId.toString());
        if(stringProfile==null) {
            return null;
        }
        try {
            profile = mapper.readValue(stringProfile, UserProfile.class);
        } catch (IOException e) {
            throw new ProfilesException(
                    "Error while getting json for user [" + userId + "]",
                    e
            );
        } finally {
            pool.returnResource(jedis);
        }
        return profile;
    }
}