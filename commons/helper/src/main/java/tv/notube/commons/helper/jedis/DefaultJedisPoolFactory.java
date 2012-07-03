package tv.notube.commons.helper.jedis;

import com.google.inject.Singleton;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Properties;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
@Singleton
public class DefaultJedisPoolFactory implements JedisPoolFactory {

    private static String ADDRESS = "address";

    private static JedisPool pool;

    public DefaultJedisPoolFactory(Properties properties) {
        if(properties == null) {
            throw new IllegalArgumentException("address properties cannot be null");
        }
        JedisPoolConfig config = new JedisPoolConfig();
        pool = new JedisPool(
                config,
                properties.getProperty(ADDRESS)
        );
    }

    public JedisPool build() {
        return pool;
    }
}