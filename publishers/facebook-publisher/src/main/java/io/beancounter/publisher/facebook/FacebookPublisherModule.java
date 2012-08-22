package io.beancounter.publisher.facebook;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import io.beancounter.commons.helper.PropertiesHelper;
import io.beancounter.commons.helper.jedis.DefaultJedisPoolFactory;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import io.beancounter.commons.model.Service;
import io.beancounter.commons.model.auth.AuthHandler;
import io.beancounter.usermanager.services.auth.DefaultServiceAuthorizationManager;
import io.beancounter.usermanager.services.auth.ServiceAuthorizationManager;
import io.beancounter.usermanager.services.auth.facebook.FacebookAuthHandler;
import io.beancounter.usermanager.services.auth.twitter.TwitterAuthHandler;
import io.beancounter.usermanager.services.auth.twitter.TwitterFactoryWrapper;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.guiceyfruit.jndi.JndiBind;
import io.beancounter.resolver.JedisResolver;
import io.beancounter.resolver.Resolver;
import io.beancounter.usermanager.JedisUserManagerImpl;
import io.beancounter.usermanager.UserManager;

import java.util.Properties;

public class FacebookPublisherModule extends CamelModuleWithMatchingRoutes {

    @Override
    protected void configure() {
        super.configure();
        bind(FacebookPublisherRoute.class);
    }

    @Provides
    @JndiBind("properties")
    PropertiesComponent propertiesComponent() {
        PropertiesComponent pc = new PropertiesComponent();
        pc.setLocation("classpath:beancounter.properties");
        return pc;
    }
}
