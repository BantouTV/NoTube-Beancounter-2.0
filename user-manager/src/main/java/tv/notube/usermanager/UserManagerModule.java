package tv.notube.usermanager;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import tv.notube.commons.helper.PropertiesHelper;
import tv.notube.commons.helper.jedis.DefaultJedisPoolFactory;
import tv.notube.commons.helper.jedis.JedisPoolFactory;
import tv.notube.resolver.JedisResolver;
import tv.notube.resolver.Resolver;
import tv.notube.usermanager.services.auth.DefaultServiceAuthorizationManager;
import tv.notube.usermanager.services.auth.ServiceAuthorizationManager;

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
