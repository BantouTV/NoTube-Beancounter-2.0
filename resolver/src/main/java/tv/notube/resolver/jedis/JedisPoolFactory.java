package tv.notube.resolver.jedis;

import redis.clients.jedis.JedisPool;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public interface JedisPoolFactory {

    public JedisPool build();

}