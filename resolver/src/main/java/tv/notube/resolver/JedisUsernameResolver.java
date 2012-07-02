package tv.notube.resolver;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.Verb;
import tv.notube.resolver.jedis.JedisPoolFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class JedisUsernameResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResolverRoute.class);

    private JedisPool pool;

    private Map<Verb, Integer> verbs;

    @Inject
    public JedisUsernameResolver(JedisPoolFactory factory, Properties properties) {
        pool = factory.build();
        verbs = new HashMap<Verb, Integer>();
        verbs.put(Verb.TWEET, Integer.parseInt((String) properties.get("redis.db.twitter"), 10));
        verbs.put(Verb.LIKE, Integer.parseInt((String) properties.get("redis.db.facebook"), 10));
    }

    public Activity resolve(Activity activity) {
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
        String serviceUsername = activity.getObject().getName();
        String beancounterUsername = jedis.get(serviceUsername);
        if(beancounterUsername==null) {
            LOGGER.error("User [{}] not found for [{}]", serviceUsername, verb);
            return null;
        }
        activity.getObject().setName(beancounterUsername);
        return activity;
    }

}