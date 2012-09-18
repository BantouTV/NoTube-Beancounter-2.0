package io.beancounter.resolver;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import io.beancounter.commons.helper.PropertiesHelper;
import io.beancounter.commons.helper.jedis.DefaultJedisPoolFactory;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import io.beancounter.commons.helper.resolver.Services;

import java.util.Properties;

/**
 * <i>Guice</i> module for {@link Resolver}.
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
