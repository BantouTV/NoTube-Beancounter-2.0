package tv.notube.profiler;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.lupedia.LUpediaNLPEngineImpl;
import tv.notube.commons.model.Interest;
import tv.notube.commons.model.UserProfile;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.Context;
import tv.notube.commons.model.activity.Tweet;
import tv.notube.commons.model.activity.Verb;
import tv.notube.profiler.rules.custom.TweetProfilingRule;
import tv.notube.profiles.InMemoryProfilesImpl;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class DefaultProfilerImplTestCase {

    private Profiler profiler;

    private Properties properties;

    private static final int LIMIT = 5;

    @BeforeTest
    public void setUp() throws ProfilerException {
        properties = new Properties();
        // look into hashtags definition
        properties.setProperty("tagdef.enable", "true");
        // tweets are more important than other
        properties.setProperty("verb.multiplier.TWEET", "10");
        // profiles are made only of top 5 interests
        properties.setProperty("interest.limit", String.valueOf(LIMIT));
        profiler = new DefaultProfilerImpl(
                new InMemoryProfilesImpl(),
                new LUpediaNLPEngineImpl(),
                null,
                properties
        );
        profiler.registerRule(Tweet.class, TweetProfilingRule.class);
    }

    @Test
    public void testMultipleRuns() throws ProfilerException,
            MalformedURLException {
        final UUID userId = UUID.randomUUID();
        // this is the compute of the first profile
        UserProfile actual = profiler.profile(userId, getFirstActivity());
        Assert.assertNotNull(actual);
        int numOfInts = actual.getInterests().size();
        Assert.assertTrue(numOfInts > 0 && numOfInts <= LIMIT);
        dumpInterests(actual, System.out);

        // push down another activity, which has only one interest in common
        actual = profiler.profile(userId, getSecondActivity());
        Assert.assertNotNull(actual);
        numOfInts = actual.getInterests().size();
        Assert.assertTrue(numOfInts > 0 && numOfInts <= LIMIT);
        dumpInterests(actual, System.out);

        // again another activity
        actual = profiler.profile(userId, getThirdActivity());
        Assert.assertNotNull(actual);
        numOfInts = actual.getInterests().size();
        Assert.assertTrue(numOfInts > 0 && numOfInts <= LIMIT);
        dumpInterests(actual, System.out);

        // add an activity that leads to no interests
        actual = profiler.profile(userId, getEmptyActivity());
        Assert.assertNotNull(actual);
        numOfInts = actual.getInterests().size();
        Assert.assertTrue(numOfInts > 0 && numOfInts <= LIMIT);
        dumpInterests(actual, System.out);
    }

    private Activity getEmptyActivity() {
        Activity activity = new Activity();
        activity.setVerb(Verb.TWEET);

        Tweet t = new Tweet();
        t.setText("sdulifysudkfh");
        activity.setObject(t);

        activity.setContext(new Context());
        return activity;
    }

    private void dumpInterests(UserProfile actual, PrintStream out) {
        for(Interest i : actual.getInterests()) {
            out.println(i.getReference() + " | " + i.getWeight());
        }
        out.println();
    }

    private Activity getFirstActivity() throws MalformedURLException {
        Activity activity = new Activity();
        activity.setVerb(Verb.TWEET);

        Tweet t = new Tweet();
        t.setText("Just arrived in London in time for #doctorwho http://www.bbc.co.uk/news/uk-18494541");
        t.addHashTag("doctorwho");
        t.addUrl(new URL("http://www.bbc.co.uk/news/uk-18494541"));
        t.setUrl(new URL("http://twitter.com/dpalmisano/statuses/23423"));
        activity.setObject(t);

        activity.setContext(new Context());
        return activity;
    }

    private Activity getSecondActivity() throws MalformedURLException {
        Activity activity = new Activity();
        activity.setVerb(Verb.TWEET);

        Tweet t = new Tweet();
        t.setText("Just arrived at the BBC!");
        t.setUrl(new URL("http://twitter.com/dpalmisano/statuses/11232"));
        activity.setObject(t);

        activity.setContext(new Context());
        return activity;
    }

    private Activity getThirdActivity() throws MalformedURLException {
        Activity activity = new Activity();
        activity.setVerb(Verb.TWEET);

        Tweet t = new Tweet();
        t.setText("Okay,leaving the BBC!");
        t.setUrl(new URL("http://twitter.com/dpalmisano/statuses/32733"));
        activity.setObject(t);

        activity.setContext(new Context());
        return activity;
    }

}
