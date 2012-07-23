package tv.notube.resolver;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import tv.notube.commons.helper.PropertiesHelper;
import tv.notube.commons.helper.jedis.DefaultJedisPoolFactory;
import tv.notube.commons.helper.jedis.JedisPoolFactory;
import tv.notube.commons.helper.resolver.Services;

import java.util.Properties;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ResolverModule extends AbstractModule {

    @Override
    protected void configure() {
        Properties redisProperties = PropertiesHelper.readFromClasspath("/redis.properties");
        Names.bindProperties(binder(), redisProperties);
        bind(JedisPoolFactory.class).to(DefaultJedisPoolFactory.class).asEagerSingleton();

        Properties properties = PropertiesHelper.readFromClasspath("/beancounter.properties");
        Services services = Services.build(properties);
        bind(Services.class).toInstance(services);
        bind(Resolver.class).to(JedisResolver.class);
    }
}
