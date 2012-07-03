package tv.notube.usermanager;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import tv.notube.commons.helper.PropertiesHelper;
import tv.notube.commons.helper.jedis.DefaultJedisPoolFactory;
import tv.notube.commons.helper.jedis.JedisPoolFactory;
import tv.notube.usermanager.services.auth.DefaultServiceAuthorizationManager;
import tv.notube.usermanager.services.auth.ServiceAuthorizationManager;
import java.util.Properties;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class UserManagerModule extends AbstractModule {

    @Override
    protected void configure() {
        Properties properties = PropertiesHelper.readFromClasspath("/sam.properties");
        ServiceAuthorizationManager sam = DefaultServiceAuthorizationManager.build(properties);
        bind(ServiceAuthorizationManager.class).toInstance(sam);
        bind(JedisPoolFactory.class).to(DefaultJedisPoolFactory.class).in(Singleton.class);
        bind(UserManager.class).to(JedisUserManagerImpl.class);
    }
}
