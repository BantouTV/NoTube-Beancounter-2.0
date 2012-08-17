package io.beancounter.usermanager;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import io.beancounter.commons.helper.PropertiesHelper;
import io.beancounter.commons.helper.jedis.DefaultJedisPoolFactory;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import io.beancounter.resolver.JedisResolver;
import io.beancounter.resolver.Resolver;
import io.beancounter.usermanager.services.auth.DefaultServiceAuthorizationManager;
import io.beancounter.usermanager.services.auth.ServiceAuthorizationManager;

import java.util.Properties;

import com.google.inject.name.Names;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class UserManagerModule extends AbstractModule {

    @Override
    protected void configure() {
        Properties properties = PropertiesHelper.readFromClasspath("/beancounter.properties");
        ServiceAuthorizationManager sam = DefaultServiceAuthorizationManager.build(properties);

        Properties redisProperties = PropertiesHelper.readFromClasspath("/redis.properties");
        Names.bindProperties(binder(), redisProperties);

        bind(ServiceAuthorizationManager.class).toInstance(sam);
        bind(JedisPoolFactory.class).to(DefaultJedisPoolFactory.class).in(Singleton.class);
        bind(Resolver.class).to(JedisResolver.class);
        bind(UserManager.class).to(JedisUserManagerImpl.class);
    }
}
