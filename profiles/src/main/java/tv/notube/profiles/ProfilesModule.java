package tv.notube.profiles;

import com.google.inject.AbstractModule;
import tv.notube.commons.helper.PropertiesHelper;
import tv.notube.commons.helper.jedis.DefaultJedisPoolFactory;
import tv.notube.commons.helper.jedis.JedisPoolFactory;

import java.util.Properties;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ProfilesModule extends AbstractModule {

    @Override
    protected void configure() {
        Properties properties = PropertiesHelper.readFromClasspath("profiler.properties");
        JedisPoolFactory jpf = new DefaultJedisPoolFactory(properties);
        bind(JedisPoolFactory.class).toInstance(jpf);
        bind(Profiles.class).to(JedisProfilesImpl.class);
    }
}
