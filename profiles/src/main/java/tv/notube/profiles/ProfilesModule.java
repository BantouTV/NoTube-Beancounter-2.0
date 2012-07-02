package tv.notube.profiles;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import tv.notube.profiles.jedis.DefaultJedisPoolFactory;
import tv.notube.profiles.jedis.JedisPoolFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ProfilesModule extends AbstractModule {

    @Override
    protected void configure() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("redis.db.profiles", "2");
        Names.bindProperties(binder(), properties);
        bind(JedisPoolFactory.class).to(DefaultJedisPoolFactory.class).in(Singleton.class);;
        bind(Profiles.class).to(JedisProfilesImpl.class);
    }
}
