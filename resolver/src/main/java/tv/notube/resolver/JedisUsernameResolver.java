package tv.notube.resolver;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import tv.notube.commons.helper.jedis.JedisPoolFactory;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.Verb;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
@Singleton
public class JedisUsernameResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(JedisUsernameResolver.class);

    private JedisPool pool;

    private Map<Verb, Integer> verbs;

    @Inject
    public JedisUsernameResolver(
            JedisPoolFactory factory,
            @Named("redisProperties") Properties properties
    ) {
        pool = factory.build();
        verbs = new HashMap<Verb, Integer>();
        verbs.put(Verb.TWEET, Integer.parseInt((String) properties.get("redis.db.twitter"), 10));
        verbs.put(Verb.LIKE, Integer.parseInt((String) properties.get("redis.db.facebook"), 10));
    }

    public UUID resolveUsername(Activity activity) {
        Verb verb = activity.getVerb();
        int database;
        try {
            database = verbs.get(verb);
        } catch (NullPointerException e) {
            LOGGER.error("Service [{}] not supported", verb, e);
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
            LOGGER.error("User [{}] not found for [{}]", serviceUsername, verb);
            return null;
        }
        return userId;
    }

}