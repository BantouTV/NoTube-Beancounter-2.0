package tv.notube.commons.helper.jedis;

import redis.clients.jedis.JedisPool;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public interface JedisPoolFactory {

    public JedisPool build();

}