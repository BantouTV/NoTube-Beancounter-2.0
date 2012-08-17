package io.beancounter.analytics;

import junit.framework.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import redis.clients.jedis.Jedis;
import io.beancounter.commons.helper.jedis.DefaultJedisPoolFactory;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import io.beancounter.commons.model.Interest;
import io.beancounter.commons.model.UserProfile;

import java.net.URI;
import java.util.*;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class JedisAnalyzerIntegrationTest {

    private Jedis jedis;

    private JedisAnalyzer analyzer;

    @BeforeSuite
    public void setUp() throws Exception {
        JedisPoolFactory factory = new DefaultJedisPoolFactory("localhost");
        jedis = factory.build().getResource();
        Properties properties = new Properties();
        properties.setProperty("redis.db.oldProfiles", "10");
        properties.setProperty("redis.db.trends", "11");
        analyzer = new JedisAnalyzer(factory, properties);
    }

    @Test
    public void testFindNewInterestEmptyProfile() throws Exception {
        jedis.flushAll();

        UserProfile up = getProfile();
        Map<String, Double> newInterests = analyzer.findNewInterests(up);

        Assert.assertTrue(newInterests.size() > 0);
        Assert.assertTrue(newInterests.containsKey("http://facebook.com"));
        Assert.assertEquals(newInterests.get("http://facebook.com"), 0.5);
        Assert.assertTrue(newInterests.containsKey("http://twitter.com"));
        Assert.assertEquals(newInterests.get("http://twitter.com"), 0.5);

        analyzer.updateTrends(up.getUserId(), newInterests);

        jedis.select(11);
        Map<String, String> trends = jedis.hgetAll("http://facebook.com");

        Assert.assertEquals(trends.get("count"), "1");
        Assert.assertEquals(trends.get("uuids"), "[\"c8f9822f-5f12-4694-82fe-18ccf8969b79\"]");

        up = getSecondProfile();
        newInterests = analyzer.findNewInterests(up);

        Assert.assertTrue(newInterests.size() > 0);
        Assert.assertTrue(newInterests.containsKey("http://facebook.com"));
        Assert.assertEquals(newInterests.get("http://facebook.com"), 0.5);
        Assert.assertTrue(newInterests.containsKey("http://twitter.com"));
        Assert.assertEquals(newInterests.get("http://twitter.com"), 0.5);

        analyzer.updateTrends(up.getUserId(), newInterests);

        jedis.select(11);
        Map<String, String> newTrends = jedis.hgetAll("http://facebook.com");

        Assert.assertEquals(newTrends.get("count"), "2");
        Assert.assertEquals(newTrends.get("uuids"), "[\"c8f9822f-5f12-4694-82fe-18ccf8969b79\",\"c8f9822f-5f12-4694-82fe-18ccf8969b80\"]");

        up = getProfile();
        newInterests = analyzer.findNewInterests(up);
        analyzer.updateTrends(up.getUserId(), newInterests);


    }

    @Test
    public void testFindNewInterestExistingProfile() throws Exception {
        jedis.flushAll();

        UserProfile up = getProfile();
        Map<String, Double> interests = analyzer.findNewInterests(up);
        Assert.assertTrue(interests.size() > 0);
        Assert.assertTrue(interests.containsKey("http://facebook.com"));
        Assert.assertEquals(interests.get("http://facebook.com"), 0.5);

        analyzer.updateTrends(up.getUserId(), interests);

        up = getUpdatedProfile();
        Map<String, Double> updatedInterests = analyzer.findNewInterests(up);
        Assert.assertTrue(updatedInterests.size() > 0);
        Assert.assertFalse(updatedInterests.containsKey("http://facebook.com"));
        Assert.assertTrue(updatedInterests.containsKey("http://lastFM.com"));
        Assert.assertEquals(updatedInterests.get("http://lastFM.com"), 0.4);
    }

    private UserProfile getProfile() throws Exception {
        UserProfile up = new UserProfile();
        up.setUserId(UUID.fromString("c8f9822f-5f12-4694-82fe-18ccf8969b79"));
        Set<Interest> interests = new HashSet<Interest>();
        Interest i1 = new Interest();
        i1.setResource(new URI("http://facebook.com"));
        i1.setWeight(0.5);
        Interest i2 = new Interest();
        i2.setResource(new URI("http://twitter.com"));
        i2.setWeight(0.5);
        interests.add(i1);
        interests.add(i2);
        up.setInterests(interests);
        return up;
    }

    private UserProfile getUpdatedProfile() throws Exception{
        UserProfile up = new UserProfile();
        up.setUserId(UUID.fromString("c8f9822f-5f12-4694-82fe-18ccf8969b79"));
        Set<Interest> interests = new HashSet<Interest>();
        Interest i1 = new Interest();
        i1.setResource(new URI("http://facebook.com"));
        i1.setWeight(0.3);
        Interest i2 = new Interest();
        i2.setResource(new URI("http://twitter.com"));
        i2.setWeight(0.3);
        Interest i3 = new Interest();
        i3.setResource(new URI("http://lastFM.com"));
        i3.setWeight(0.4);
        interests.add(i1);
        interests.add(i2);
        interests.add(i3);
        up.setInterests(interests);
        return up;
    }

    private UserProfile getSecondProfile() throws Exception {
        UserProfile up = new UserProfile();
        up.setUserId(UUID.fromString("c8f9822f-5f12-4694-82fe-18ccf8969b80"));
        Set<Interest> interests = new HashSet<Interest>();
        Interest i1 = new Interest();
        i1.setResource(new URI("http://facebook.com"));
        i1.setWeight(0.5);
        Interest i2 = new Interest();
        i2.setResource(new URI("http://twitter.com"));
        i2.setWeight(0.5);
        interests.add(i1);
        interests.add(i2);
        up.setInterests(interests);
        return up;
    }


}