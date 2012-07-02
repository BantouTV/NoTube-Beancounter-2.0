package tv.notube.applications;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import tv.notube.applications.jedis.DefaultJedisPoolFactory;
import tv.notube.applications.jedis.JedisPoolFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ApplicationsModule extends AbstractModule {

    @Override
    protected void configure() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("redis.db.application", "1");
        Names.bindProperties(binder(), properties);
        bind(JedisPoolFactory.class).to(DefaultJedisPoolFactory.class).asEagerSingleton();
        bind(ApplicationsManager.class).to(JedisApplicationsManagerImpl.class);
    }
}
