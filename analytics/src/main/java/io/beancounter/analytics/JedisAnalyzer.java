package io.beancounter.analytics;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import io.beancounter.commons.model.Interest;
import io.beancounter.commons.model.UserProfile;

import java.io.IOException;
import java.util.*;

/**
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class JedisAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JedisAnalyzer.class);

    private JedisPool pool;

    private int oldProfilesDB;

    private int trendsDB;

    private static final long MAX_TREND_TIME = 1000 * 60 * 60 * 36;

    @Inject
    public JedisAnalyzer(
            JedisPoolFactory factory,
            @Named("redisProperties") Properties properties
    ) {
        pool = factory.build();
        oldProfilesDB = Integer.parseInt((String) properties.get("redis.db.oldProfiles"), 10);
        trendsDB = Integer.parseInt((String) properties.get("redis.db.trends"), 10);
    }

    /*
     *
     * Don't touch: magic.
     *
     */

    public Map<String, Double> findNewInterests(UserProfile userProfile) {

        // the interest in the actual profile are the new ones
        Map<String, Double> newInterests = new HashMap<String, Double>();
        for (Interest i : userProfile.getInterests()) {
            newInterests.put(i.getResource().toString(), i.getWeight());
        }

        Jedis jedis = pool.getResource();
        jedis.select(oldProfilesDB);

        // retrieve the old interests from redis
        Map<String, String> oldInterests = jedis.hgetAll(userProfile.getUserId().toString());

        Map<String, Double> updatedInterests = new HashMap<String, Double>();

        // if the user didn't have any let's just store the new one and return this!
        if (oldInterests.size()==0) {
            updatedInterests = newInterests;

        } else {

            // if the user has something old check what has been changed since the last time
            for (Map.Entry entry : newInterests.entrySet()) {

                String interest = (String) entry.getKey();
                double newInterestWeight = (Double) entry.getValue();

                if (oldInterests.containsKey(interest)) {
                    double oldInterestWeight = Double.parseDouble( oldInterests.get( interest ));
                    // if the weight of the interest is greater means that that interest has been "triggered"
                    if (newInterestWeight >= oldInterestWeight) {
                        updatedInterests.put( interest, newInterestWeight);
                    }

                } else {
                    // if the interest is not contained in the old interests, it's a new one!
                    updatedInterests.put( interest, newInterestWeight );
                }
            }
        }

        // now we have the updated interests. Let's store the new one and return the updated
        for (Map.Entry e : newInterests.entrySet()) {
            jedis.hset(
                    userProfile.getUserId().toString(),
                    e.getKey().toString(),
                    e.getValue().toString()
            );
        }

        pool.returnResource(jedis);
        return updatedInterests;
    }


    public void updateTrends(UUID userId, Map<String, Double> newInterests) throws JedisAnalyzerException {
        ObjectMapper mapper = new ObjectMapper();
        Set<String> uuids;

        Jedis jedis = null;

        try {
            jedis = pool.getResource();
            jedis.select(trendsDB);

            for (Map.Entry entry : newInterests.entrySet()) {
                String interest = entry.getKey().toString();
                // for each new trend we get its fields
                Map<String, String> serviceTrends = jedis.hgetAll(entry.getKey().toString());

                // if size==0 the trend is a new one -> store it!
                if (serviceTrends.size() == 0) {
                    uuids = new HashSet<String>();
                    uuids.add(userId.toString());
                    storeHset(jedis, interest, DateTime.now().getMillis(), 1, uuids);

                } else if((DateTime.now().getMillis()-Long.valueOf(serviceTrends.get("timestamp"))) > MAX_TREND_TIME) {
                    // se il tempo trascorso e' maggiore di quello consentito trattiamo il trend come "nuovo"
                    uuids = new HashSet<String>();
                    uuids.add(userId.toString());
                    storeHset(jedis, interest, DateTime.now().getMillis(), 1, uuids);

                } else {
                    try {
                        uuids = mapper.readValue(serviceTrends.get("uuids"), Set.class);
                    } catch (IOException e) {
                        throw new JedisAnalyzerException("error while parsing the list of uuid", e);
                    }
                    uuids.add(userId.toString());
                    int count = Integer.parseInt(serviceTrends.get("count"));
                    count++;
                    storeHset(jedis, interest, DateTime.now().getMillis(), count, uuids);
                }
            }
        } catch (JedisAnalyzerException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } finally {
            pool.returnResource(jedis);
        }

    }


    private void storeHset(Jedis jedis, String interest, long dateMillis, int count, Set<String> uuids) throws JedisAnalyzerException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String uuidsStr = mapper.writeValueAsString(uuids);
            jedis.hset(interest, "timestamp", String.valueOf(dateMillis));
            jedis.hset(interest, "count", String.valueOf(count));
            jedis.hset(interest, "uuids", uuidsStr);
        } catch (IOException e) {
            throw new JedisAnalyzerException("error while parsing the list of uuid", e);
        }
    }
}