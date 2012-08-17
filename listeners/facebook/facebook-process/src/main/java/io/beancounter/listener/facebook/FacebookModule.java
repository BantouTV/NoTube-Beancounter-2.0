package io.beancounter.listener.facebook;

import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.restfb.types.Post;
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

        Properties properties = PropertiesHelper.readFromClasspath("/beancounter.properties");
        Services services = Services.build(properties);
        bind(Services.class).toInstance(services);
        bind(Resolver.class).to(JedisResolver.class);

        ServiceAuthorizationManager sam = DefaultServiceAuthorizationManager.build(properties);

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