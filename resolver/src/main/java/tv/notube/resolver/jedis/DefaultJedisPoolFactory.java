package tv.notube.resolver.jedis;

import com.google.inject.Singleton;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
@Singleton
public class DefaultJedisPoolFactory implements JedisPoolFactory {

    public DefaultJedisPoolFactory() {
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