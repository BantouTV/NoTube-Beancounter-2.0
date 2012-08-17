package tv.notube.resolver.process;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.guiceyfruit.jndi.JndiBind;
import tv.notube.commons.helper.PropertiesHelper;
import tv.notube.commons.helper.jedis.DefaultJedisPoolFactory;
import tv.notube.commons.helper.jedis.JedisPoolFactory;
import tv.notube.commons.helper.resolver.Services;
import tv.notube.commons.model.Service;
import tv.notube.commons.model.auth.AuthHandler;
import tv.notube.resolver.JedisResolver;
import tv.notube.resolver.Resolver;
import tv.notube.usermanager.JedisUserManagerImpl;
import tv.notube.usermanager.UserManager;
import tv.notube.usermanager.services.auth.DefaultServiceAuthorizationManager;
import tv.notube.usermanager.services.auth.ServiceAuthorizationManager;
import tv.notube.usermanager.services.auth.facebook.FacebookAuthHandler;
import tv.notube.usermanager.services.auth.twitter.TwitterAuthHandler;
import tv.notube.usermanager.services.auth.twitter.TwitterFactoryWrapper;

import java.util.Properties;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class ResolverModule extends CamelModuleWithMatchingRoutes {

    @Override
    protected void configure() {
        super.configure();
        Properties redisProperties = PropertiesHelper.readFromClasspath("/redis.properties");
        Names.bindProperties(binder(), redisProperties);
        bindInstance("redisProperties", redisProperties);
        bind(JedisPoolFactory.class).to(DefaultJedisPoolFactory.class).asEagerSingleton();
        bind(TwitterFactoryWrapper.class).in(Singleton.class);

        Properties properties = PropertiesHelper.readFromClasspath("/beancounter.properties");
        Services services = Services.build(properties);

        Service twitterService = DefaultServiceAuthorizationManager.buildService("twitter", properties);
        Service facebookService = DefaultServiceAuthorizationManager.buildService("facebook", properties);
        bind(Service.class)
                .annotatedWith(Names.named("service.twitter"))
                .toInstance(twitterService);
        bind(Service.class)
                .annotatedWith(Names.named("service.facebook"))
                .toInstance(facebookService);

        MapBinder<Service, AuthHandler> authHandlerBinder
                = MapBinder.newMapBinder(binder(), Service.class, AuthHandler.class);
        authHandlerBinder.addBinding(twitterService).to(TwitterAuthHandler.class);
        authHandlerBinder.addBinding(facebookService).to(FacebookAuthHandler.class);

        bind(UserManager.class).to(JedisUserManagerImpl.class).asEagerSingleton();
        bind(Services.class).toInstance(services);
        bind(Resolver.class).to(JedisResolver.class);
        bind(ServiceAuthorizationManager.class).to(DefaultServiceAuthorizationManager.class);

        bind(ResolverRoute.class);
    }

    @Provides
    @JndiBind("properties")
    PropertiesComponent propertiesComponent() {
        PropertiesComponent pc = new PropertiesComponent();
        pc.setLocation("classpath:beancounter.properties");
        return pc;
    }


}