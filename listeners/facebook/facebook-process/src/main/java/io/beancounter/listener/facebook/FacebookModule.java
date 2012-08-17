package io.beancounter.listener.facebook;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import com.restfb.types.Post;
import io.beancounter.commons.model.Service;
import io.beancounter.commons.model.auth.AuthHandler;
import io.beancounter.usermanager.services.auth.facebook.FacebookAuthHandler;
import io.beancounter.usermanager.services.auth.twitter.TwitterAuthHandler;
import io.beancounter.usermanager.services.auth.twitter.TwitterFactoryWrapper;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.guiceyfruit.jndi.JndiBind;
import io.beancounter.commons.helper.PropertiesHelper;
import io.beancounter.commons.helper.jedis.DefaultJedisPoolFactory;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import io.beancounter.commons.helper.resolver.Services;
import io.beancounter.commons.model.activity.Verb;
import io.beancounter.listener.commons.ActivityConverter;
import io.beancounter.listener.facebook.core.converter.FacebookActivityConverter;
import io.beancounter.listener.facebook.core.converter.FacebookActivityConverterException;
import io.beancounter.listener.facebook.core.converter.custom.FacebookLikeConverter;
import io.beancounter.listener.facebook.core.converter.custom.FacebookShareConverter;
import io.beancounter.listener.facebook.core.model.FacebookData;
import io.beancounter.resolver.JedisResolver;
import io.beancounter.resolver.Resolver;
import io.beancounter.usermanager.JedisUserManagerImpl;
import io.beancounter.usermanager.UserManager;
import io.beancounter.usermanager.services.auth.DefaultServiceAuthorizationManager;
import io.beancounter.usermanager.services.auth.ServiceAuthorizationManager;

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