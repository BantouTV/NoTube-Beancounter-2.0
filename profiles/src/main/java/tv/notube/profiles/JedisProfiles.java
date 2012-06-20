package tv.notube.profiles;

import org.codehaus.jackson.map.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import tv.notube.commons.model.UserProfile;
import tv.notube.profiles.jedis.JedisPoolFactory;

import java.util.UUID;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class JedisProfiles implements Profiles {

    private JedisPool pool;

    private ObjectMapper mapper;

    public JedisProfiles(JedisPoolFactory factory) {
        pool = factory.build();
        mapper = new ObjectMapper();
    }

    @Override
    public void store(UserProfile up) throws ProfilesException {
        Jedis jedis = null;
        try {
            String userProfile = mapper.writeValueAsString(up);
            jedis = pool.getResource();
            jedis.set(up.getId().toString(),userProfile);
        } catch (Exception e) {
            throw new ProfilesException(
                    "Error while storing the profile for user: " + up.getId().toString(),
                    e);
        } finally {
            if(jedis!=null)
                pool.returnResource(jedis);
        }
    }

    @Override
    public UserProfile lookup(UUID userId) throws ProfilesException {
        UserProfile profile;
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            String stringProfile = jedis.get(userId.toString());
            profile = mapper.readValue(stringProfile, UserProfile.class);
        } catch (Exception e) {
            throw new ProfilesException(
                    "Error while retrieving the profile for user: " + userId.toString(),
                    e);
        } finally {
            if(jedis!=null)
                pool.returnResource(jedis);
        }
        return profile;
    }
}