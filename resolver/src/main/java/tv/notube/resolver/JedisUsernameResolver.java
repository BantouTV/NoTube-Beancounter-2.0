package tv.notube.resolver;

import com.google.inject.Inject;
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
public class JedisUsernameResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResolverRoute.class);

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
        // TODO (med) this is a workaround to by pass the resolver
        // for our sally demo app.
        /*
        final URL service;
        try {
            service = new URL("http://sally.beancounter.io");
        } catch (MalformedURLException e) {
            throw new RuntimeException("http://sally.beancounter.io is ill formed", e);
        }
        if (activity.getContext().getService().equals(service)) {
            activity.getObject().setName("sally-beancounter");
            // TODO (med) this is another workaround for the sally demo
            return UUID.fromString("363edc3d-1629-454a-b1b6-1c4de4537a6f");
        }
        */
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
        String userIdStr = jedis.get(serviceUsername);
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