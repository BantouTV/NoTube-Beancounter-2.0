package tv.notube.usermanager;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import tv.notube.commons.model.Service;
import tv.notube.usermanager.jedis.DefaultJedisPoolFactory;
import tv.notube.usermanager.jedis.JedisPoolFactory;
import tv.notube.usermanager.services.auth.DefaultServiceAuthorizationManager;
import tv.notube.usermanager.services.auth.ServiceAuthorizationManager;
import tv.notube.usermanager.services.auth.ServiceAuthorizationManagerException;
import tv.notube.usermanager.services.auth.twitter.TwitterAuthHandler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class UserManagerModule extends AbstractModule {

    @Override
    protected void configure() {
        ServiceAuthorizationManager sam = new DefaultServiceAuthorizationManager();
        Service twitter = new Service("twitter");
        twitter.setDescription("Twitter service");
        try {
            twitter.setEndpoint(
                    new URL("https://api.twitter.com/1/statuses/user_timeline.json")
            );
            twitter.setSessionEndpoint(new URL("https://api.twitter.com/oauth/request_token"));
        } catch (MalformedURLException e) {
            // com'on.
        }
        // TODO (really high) this must be configurable
        twitter.setApikey("Vs9UkC1ZhE3pT9P4JwbA");
        twitter.setSecret("BRDzw6MFJB3whzmm1rWlzjsD5LoXJmlmYT40lhravRs");
        try {
            sam.addHandler(
                    twitter,
                    new TwitterAuthHandler(twitter)
            );
        } catch (ServiceAuthorizationManagerException e) {
            final String errMsg = "error while adding twitter to this stuff";
            throw new RuntimeException(errMsg, e);
        }
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("redis.db.users", "0");
        Names.bindProperties(binder(), properties);
        bind(ServiceAuthorizationManager.class).toInstance(sam);
        bind(JedisPoolFactory.class).to(DefaultJedisPoolFactory.class).in(Singleton.class);;
        bind(UserManager.class).to(JedisUserManagerImpl.class);
    }
}
