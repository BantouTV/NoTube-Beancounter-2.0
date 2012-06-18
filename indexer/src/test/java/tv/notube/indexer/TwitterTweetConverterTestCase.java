package tv.notube.indexer;

import junit.framework.Assert;
import org.testng.annotations.Test;
import tv.notube.commons.model.activity.*;
import tv.notube.crawler.requester.ServiceResponseException;
import tv.notube.crawler.requester.request.twitter.TwitterTweet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class TwitterTweetConverterTestCase {

    @Test
    public void testConvert() throws MalformedURLException, ServiceResponseException {

        TwitterTweet tweet = new TwitterTweet();
        Set<String> hashTags =  new HashSet<String>();
        hashTags.add("firstTag");
        tweet.setHashTags(hashTags);

        TwitterTweetConverter converter = new TwitterTweetConverter();
        Activity activity = converter.convert(tweet);

        Assert.assertEquals(activity.getVerb(), Verb.TWEET);
        Tweet object = (Tweet) activity.getObject();
        Assert.assertTrue(object.getHashTags().contains("firstTag"));
        Assert.assertEquals(activity.getContext().getService(), new URL("http://twitter.com"));
    }

}