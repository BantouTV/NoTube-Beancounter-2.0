package tv.notube.resolver.process;

import com.google.inject.Provides;
import com.google.inject.name.Names;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.guiceyfruit.jndi.JndiBind;
import tv.notube.commons.helper.PropertiesHelper;
import tv.notube.commons.helper.jedis.DefaultJedisPoolFactory;
import tv.notube.commons.helper.jedis.JedisPoolFactory;
import tv.notube.commons.helper.resolver.Services;
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
public class ResolverModule extends CamelModuleWithMatchingRoutes {

    @Override
    protected void configure() {
        super.configure();
        Properties redisProperties = PropertiesHelper.readFromClasspath("/redis.properties");
        Names.bindProperties(binder(), redisProperties);
        bindInstance("redisProperties", redisProperties);
        bind(JedisPoolFactory.class).to(DefaultJedisPoolFactory.class).asEagerSingleton();

        Properties properties = PropertiesHelper.readFromClasspath("/beancounter.properties");
        Services services = Services.build(properties);

        ServiceAuthorizationManager sam = DefaultServiceAuthorizationManager.build(properties);
        bind(ServiceAuthorizationManager.class).toInstance(sam);

        bind(UserManager.class).to(JedisUserManagerImpl.class).asEagerSingleton();
        bind(Services.class).toInstance(services);
        bind(Resolver.class).to(JedisResolver.class);

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