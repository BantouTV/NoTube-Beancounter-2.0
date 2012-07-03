package tv.notube.commons.helper.jedis;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
@Singleton
public class DefaultJedisPoolFactory implements JedisPoolFactory {

    private static JedisPool pool;

    @Inject
    public DefaultJedisPoolFactory(@Named("address") String address) {
        JedisPoolConfig config = new JedisPoolConfig();
        pool = new JedisPool(
                config,
                address
        );
    }

    public JedisPool build() {
        return pool;
    }
}