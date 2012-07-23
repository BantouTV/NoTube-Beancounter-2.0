package tv.notube.analytics;

import com.google.inject.Provides;
import com.google.inject.name.Names;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.guiceyfruit.jndi.JndiBind;
import tv.notube.commons.helper.PropertiesHelper;
import tv.notube.commons.helper.jedis.DefaultJedisPoolFactory;
import tv.notube.commons.helper.jedis.JedisPoolFactory;

import java.util.Properties;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class AnalyticsModule extends CamelModuleWithMatchingRoutes {

    @Override
    protected void configure() {
        super.configure();
        Properties properties = PropertiesHelper.readFromClasspath("/redis.properties");
        Names.bindProperties(binder(), properties);
        bind(JedisPoolFactory.class).to(DefaultJedisPoolFactory.class).asEagerSingleton();
        bindInstance("redisProperties", properties);
        bind(JedisAnalyzer.class).asEagerSingleton();
        bind(AnalyticsRoute.class);
    }

    @Provides
    @JndiBind("properties")
    PropertiesComponent propertiesComponent() {
        PropertiesComponent pc = new PropertiesComponent();
        pc.setLocation("classpath:resolver.properties");
        return pc;
    }

}