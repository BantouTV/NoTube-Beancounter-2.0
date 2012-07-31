package tv.notube.listener.facebook;

import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.restfb.types.Post;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.guiceyfruit.jndi.JndiBind;
import tv.notube.commons.helper.PropertiesHelper;
import tv.notube.commons.helper.jedis.DefaultJedisPoolFactory;
import tv.notube.commons.helper.jedis.JedisPoolFactory;
import tv.notube.commons.helper.resolver.Services;
import tv.notube.commons.model.activity.Verb;
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

        Properties properties = PropertiesHelper.readFromClasspath("/beancounter.properties");
        Services services = Services.build(properties);
        bind(Services.class).toInstance(services);
        bind(Resolver.class).to(JedisResolver.class);

        Properties samProperties = PropertiesHelper.readFromClasspath("/sam.properties");
        ServiceAuthorizationManager sam = DefaultServiceAuthorizationManager.build(samProperties);

        bind(ServiceAuthorizationManager.class).toInstance(sam);
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