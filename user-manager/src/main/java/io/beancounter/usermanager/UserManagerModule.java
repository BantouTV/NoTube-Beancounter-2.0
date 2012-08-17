package io.beancounter.usermanager;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import io.beancounter.commons.helper.PropertiesHelper;
import io.beancounter.commons.helper.jedis.DefaultJedisPoolFactory;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import io.beancounter.commons.model.Service;
import io.beancounter.commons.model.auth.AuthHandler;
import io.beancounter.resolver.JedisResolver;
import io.beancounter.resolver.Resolver;
import io.beancounter.usermanager.services.auth.DefaultServiceAuthorizationManager;
import io.beancounter.usermanager.services.auth.ServiceAuthorizationManager;
import io.beancounter.usermanager.services.auth.facebook.FacebookAuthHandler;
import io.beancounter.usermanager.services.auth.twitter.TwitterAuthHandler;
import io.beancounter.usermanager.services.auth.twitter.TwitterFactoryWrapper;

import java.util.Properties;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class UserManagerModule extends AbstractModule {

    @Override
    protected void configure() {
        Properties properties = PropertiesHelper.readFromClasspath("/beancounter.properties");
        Properties redisProperties = PropertiesHelper.readFromClasspath("/redis.properties");
        Names.bindProperties(binder(), redisProperties);

        Service twitterService = DefaultServiceAuthorizationManager.buildService("twitter", properties);
        Service facebookService = DefaultServiceAuthorizationManager.buildService("facebook", properties);
        bind(Service.class)
                .annotatedWith(Names.named("service.twitter"))
                .toInstance(twitterService);
        bind(Service.class)
                .annotatedWith(Names.named("service.facebook"))
                .toInstance(facebookService);

        bind(JedisPoolFactory.class).to(DefaultJedisPoolFactory.class).in(Singleton.class);
        bind(TwitterFactoryWrapper.class).in(Singleton.class);
        bind(Resolver.class).to(JedisResolver.class);
        bind(UserManager.class).to(JedisUserManagerImpl.class);

        MapBinder<Service, AuthHandler> authHandlerBinder
                = MapBinder.newMapBinder(binder(), Service.class, AuthHandler.class);
        authHandlerBinder.addBinding(twitterService).to(TwitterAuthHandler.class);
        authHandlerBinder.addBinding(facebookService).to(FacebookAuthHandler.class);

        bind(ServiceAuthorizationManager.class).to(DefaultServiceAuthorizationManager.class);
    }
}
