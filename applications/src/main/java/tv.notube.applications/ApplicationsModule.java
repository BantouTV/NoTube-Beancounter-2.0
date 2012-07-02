package tv.notube.applications;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import tv.notube.applications.jedis.DefaultJedisPoolFactory;
import tv.notube.applications.jedis.JedisPoolFactory;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ApplicationsModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(JedisPoolFactory.class).to(DefaultJedisPoolFactory.class).in(Singleton.class);;
        bind(ApplicationsManager.class).to(JedisApplicationsManagerImpl.class);
    }
}
