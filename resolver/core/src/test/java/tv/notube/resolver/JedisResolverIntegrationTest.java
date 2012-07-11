package tv.notube.resolver;

import com.google.inject.Guice;
import com.google.inject.Injector;
import junit.framework.Assert;
import org.joda.time.DateTime;
import org.testng.annotations.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import tv.notube.commons.helper.jedis.JedisPoolFactory;
import tv.notube.commons.model.activity.*;

import java.lang.Object;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class JedisResolverIntegrationTest {

    private Resolver resolver;

    private Jedis jedis;

    @BeforeSuite
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new ResolverModule());
        JedisPool pool = injector.getInstance(JedisPoolFactory.class).build();
        jedis = pool.getResource();
        resolver = injector.getInstance(Resolver.class);
    }

    @BeforeMethod
    public void flush() {
        jedis.flushAll();
    }

    @Test
    public void testResolveTweet() throws Exception {
        jedis.select(3);
        UUID uuid = UUID.randomUUID();
        String value = UserValues.unparse(new UserValues(uuid, "beancounter-username"));
        jedis.set("twitter_username", value);
        Activity tweet = getTweetActivity();
        UUID resolvedUserId = resolver.resolve(tweet);
        Assert.assertEquals(resolvedUserId, uuid);
    }

    @Test
    public void testResolveFacebookEvent() throws Exception {
        jedis.select(4);
        UUID uuid = UUID.randomUUID();
        String value = UserValues.unparse(new UserValues(uuid, "beancounter-username"));
        jedis.set("facebook_username", value);
        Activity event = getEventActivity();
        UUID resolvedUserId = resolver.resolve(event);
        Assert.assertEquals(resolvedUserId, uuid);
    }

    @Test
    public void testResolveFacebookEventNotStoredUser() throws Exception {
        jedis.select(4);
        UUID uuid = UUID.randomUUID();
        String value = UserValues.unparse(new UserValues(uuid, "beancounter-username"));
        jedis.set("facebook_username", value);
        Activity event = getEventActivity();
        event.getContext().setUsername("not-existing-user");
        UUID resolved = resolver.resolve(event);
        Assert.assertNull(resolved);
    }

    @Test
    public void testResolveNotSupportedActivity() throws Exception {
        jedis.select(3);
        jedis.set("something_username","beancounter");
        Activity unsupportedActivity = getEventActivity();
        unsupportedActivity.setVerb(Verb.CHECKIN);
        UUID resolved = resolver.resolve(unsupportedActivity);
        Assert.assertNull(resolved);
    }

    private Activity getTweetActivity() throws Exception {
        ActivityBuilder builder = new DefaultActivityBuilder();
        builder.push();
        builder.setVerb(Verb.TWEET);
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("setText", "Some fake text");
        builder.setObject(Tweet.class, new URL("http://test.com"), "Tweet Name", fields);
        builder.setContext(new DateTime(), "twitter", "twitter_username");
        return builder.pop();
    }

    private Activity getEventActivity() throws Exception {
        ActivityBuilder builder = new DefaultActivityBuilder();
        builder.push();
        builder.setVerb(Verb.LIKE);
        Map<String, Object> fields = new HashMap<String, Object>();
        builder.setObject(Event.class, new URL("http://test.com"), "Event Name", fields);
        builder.setContext(new DateTime(), "facebook", "facebook_username");
        return builder.pop();
    }

}