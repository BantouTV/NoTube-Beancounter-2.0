package tv.notube.profiles.jedis;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class DefaultJedisPoolFactory implements JedisPoolFactory {

    private static JedisPoolFactory instance;

    public static synchronized JedisPoolFactory getInstance() {
        if(instance==null) {
            instance = new DefaultJedisPoolFactory();
        }
        return instance;
    }

    private DefaultJedisPoolFactory() {
        JedisPoolConfig config = new JedisPoolConfig();
        String address = "localhost";
        pool = new JedisPool(config, address);
    }

    private static JedisPool pool;

    @Override
    public JedisPool build() {
        return pool;
    }
}