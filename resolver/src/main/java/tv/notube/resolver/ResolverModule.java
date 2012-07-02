package tv.notube.resolver;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import tv.notube.resolver.jedis.DefaultJedisPoolFactory;
import tv.notube.resolver.jedis.JedisPoolFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class ResolverModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(JedisPoolFactory.class).to(DefaultJedisPoolFactory.class).asEagerSingleton();
    }



}