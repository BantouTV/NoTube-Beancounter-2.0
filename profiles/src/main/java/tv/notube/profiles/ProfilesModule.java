package tv.notube.profiles;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import tv.notube.profiles.jedis.DefaultJedisPoolFactory;
import tv.notube.profiles.jedis.JedisPoolFactory;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ProfilesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(JedisPoolFactory.class).to(DefaultJedisPoolFactory.class).in(Singleton.class);;
        bind(Profiles.class).to(JedisProfilesImpl.class);
    }
}
