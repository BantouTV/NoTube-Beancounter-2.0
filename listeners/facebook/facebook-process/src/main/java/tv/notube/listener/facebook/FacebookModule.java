package tv.notube.listener.facebook;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import com.restfb.types.Post;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.guiceyfruit.jndi.JndiBind;
import tv.notube.commons.helper.PropertiesHelper;
import tv.notube.commons.helper.jedis.DefaultJedisPoolFactory;
import tv.notube.commons.helper.jedis.JedisPoolFactory;
import tv.notube.commons.helper.resolver.Services;
import tv.notube.commons.model.Service;
import tv.notube.commons.model.activity.Verb;
import tv.notube.commons.model.auth.AuthHandler;
import tv.notube.listener.commons.ActivityConverter;
import tv.notube.listener.facebook.core.converter.FacebookActivityConverter;
import tv.notube.listener.facebook.core.converter.FacebookActivityConverterException;
import tv.notube.listener.facebook.core.converter.custom.FacebookLikeConverter;
import tv.notube.listener.facebook.core.converter.custom.FacebookShareConverter;
import tv.notube.listener.facebook.core.model.FacebookData;
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
public class FacebookModule extends CamelModuleWithMatchingRoutes {

    public void configure() {
        super.configure();
        Properties redisProperties = PropertiesHelper.readFromClasspath("/redis.properties");
        Names.bindProperties(binder(), redisProperties);
        bindInstance("redisProperties", redisProperties);
        bind(JedisPoolFactory.class).to(DefaultJedisPoolFactory.class).asEagerSingleton();
        bind(TwitterFactoryWrapper.class).in(Singleton.class);

        Properties properties = PropertiesHelper.readFromClasspath("/beancounter.properties");
        Services services = Services.build(properties);
        bind(Services.class).toInstance(services);
        bind(Resolver.class).to(JedisResolver.class);

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

        bind(ServiceAuthorizationManager.class).to(DefaultServiceAuthorizationManager.class);
        bind(UserManager.class).to(JedisUserManagerImpl.class);
        FacebookActivityConverter fac = new FacebookActivityConverter();
        try {
            fac.registerConverter(Post.class, Verb.SHARE, new FacebookShareConverter());
            fac.registerConverter(FacebookData.class, Verb.LIKE, new FacebookLikeConverter());
        } catch (FacebookActivityConverterException e) {
            throw new RuntimeException("Error while instantiating Facebook converters", e);
        }
        bind(FacebookActivityConverter.class).toInstance(fac);
        bind(ActivityConverter.class).to(FacebookNotificationConverter.class);
        bind(FacebookRoute.class);
    }

    @Provides
    @JndiBind("properties")
    PropertiesComponent propertiesComponent() {
        PropertiesComponent pc = new PropertiesComponent();
        pc.setLocation("classpath:beancounter.properties");
        return pc;
    }

}