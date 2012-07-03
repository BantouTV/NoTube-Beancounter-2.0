package tv.notube.resolver;

import com.google.inject.Guice;
import com.google.inject.Injector;
import junit.framework.Assert;
import org.joda.time.DateTime;
import org.testng.annotations.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import tv.notube.commons.model.activity.*;
import tv.notube.resolver.jedis.JedisPoolFactory;

import java.io.IOException;
import java.lang.Object;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class JedisUsernameResolverIntegrationTest {

    private JedisUsernameResolver resolver;

    private Jedis jedis;

    @BeforeSuite
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new ResolverModule());
        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream("/redis.properties"));
        resolver = new JedisUsernameResolver(injector.getInstance(JedisPoolFactory.class), properties);
        JedisPool pool = injector.getInstance(JedisPoolFactory.class).build();
        jedis = pool.getResource();
    }

    @BeforeMethod
    public void flush() {
        jedis.flushAll();
    }

    @Test
    public void testResolveTweet() throws Exception {
        jedis.select(3);
        jedis.set("twitter_username","beancounter");
        Activity tweet = getTweetActivity();
        Activity resolved = resolver.resolve(tweet);
        Assert.assertEquals(resolved.getObject().getName(), "beancounter");
    }

    @Test
    public void testResolveFacebookEvent() throws Exception {
        jedis.select(4);
        jedis.set("facebook_username","beancounter");
        Activity tweet = getEventActivity();
        Activity resolved = resolver.resolve(tweet);
        Assert.assertEquals(resolved.getObject().getName(), "beancounter");
    }

    @Test
    public void testResolveFacebookEventNotStoredUser() throws Exception {
        jedis.select(4);
        jedis.set("facebook_username","beancounter");
        Activity event = getEventActivity();
        event.getObject().setName("not-existing-user");
        Activity resolved = resolver.resolve(event);
        Assert.assertNull(resolved);
    }

    @Test
    public void testResolveNotSupportedActivity() throws Exception {
        jedis.select(3);
        jedis.set("something_username","beancounter");
        Activity tweet = getEventActivity();
        tweet.setVerb(Verb.CHECKIN);
        Activity resolved = resolver.resolve(tweet);
        Assert.assertNull(resolved);
    }

    private Activity getTweetActivity() throws Exception {
        ActivityBuilder builder = new DefaultActivityBuilder();
        builder.push();
        builder.setVerb(Verb.TWEET);
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("setText", "Some fake text");
        builder.setObject(Tweet.class, new URL("http://test.com"), "twitter_username", fields);
        builder.setContext(new DateTime(), new URL("http://service.com"));
        return builder.pop();
    }

    private Activity getEventActivity() throws Exception {
        ActivityBuilder builder = new DefaultActivityBuilder();
        builder.push();
        builder.setVerb(Verb.LIKE);
        Map<String, Object> fields = new HashMap<String, Object>();
        builder.setObject(Event.class, new URL("http://test.com"), "facebook_username", fields);
        builder.setContext(new DateTime(), new URL("http://service.com"));
        return builder.pop();
    }

}