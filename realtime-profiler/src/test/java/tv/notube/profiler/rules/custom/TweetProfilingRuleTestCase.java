package tv.notube.profiler.rules.custom;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.lupedia.LUpediaNLPEngineImpl;
import tv.notube.commons.model.activity.Tweet;
import tv.notube.profiler.rules.ObjectProfilingRule;
import tv.notube.profiler.rules.ProfilingRuleException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Properties;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TweetProfilingRuleTestCase {

    private ObjectProfilingRule<Tweet> rule;

    private Properties properties;

    @BeforeTest
    public void setUp() {
        properties = new Properties();
        properties.setProperty("tagdef.enable", "true");
    }

    @Test
    public void testJustText() throws ProfilingRuleException, URISyntaxException, MalformedURLException {
        rule = new TweetProfilingRule(
                getSimpleTweet(),
                new LUpediaNLPEngineImpl(),
                null
        );
        rule.run(properties);
        Collection<URI> actual = rule.getResult();
        Assert.assertEquals(actual.size(), 1);
        Assert.assertTrue(actual.contains(new URI("http://dbpedia.org/resource/London")));
    }

    @Test
    public void testTextAndAnHashTag() throws ProfilingRuleException, URISyntaxException, MalformedURLException {
        rule = new TweetProfilingRule(
                getTweetWithHashTag(),
                new LUpediaNLPEngineImpl(),
                null
        );
        rule.run(properties);
        Collection<URI> actual = rule.getResult();
        Assert.assertEquals(actual.size(), 4);
        Assert.assertTrue(actual.contains(new URI("http://dbpedia.org/resource/London")));
        Assert.assertTrue(actual.contains(new URI("http://dbpedia.org/resource/Doctor_Who")));
        Assert.assertTrue(actual.contains(new URI("http://dbpedia.org/resource/BBC")));
    }

    @Test
    public void testTextAnHashTagAndAnURL() throws ProfilingRuleException,
            URISyntaxException, MalformedURLException {
        rule = new TweetProfilingRule(
                getTweetWithHashTagAndUrl(),
                new LUpediaNLPEngineImpl(),
                null
        );
        rule.run(properties);
        Collection<URI> actual = rule.getResult();
        Assert.assertEquals(actual.size(), 7);
        Assert.assertTrue(actual.contains(new URI("http://dbpedia.org/resource/London")));
        Assert.assertTrue(actual.contains(new URI("http://dbpedia.org/resource/Doctor_Who")));
        Assert.assertTrue(actual.contains(new URI("http://dbpedia.org/resource/BBC")));
        Assert.assertTrue(actual.contains(new URI("http://dbpedia.org/resource/Cardiff")));
        Assert.assertTrue(actual.contains(new URI("http://dbpedia.org/resource/Barnsley")));
    }

    private Tweet getTweetWithHashTagAndUrl() throws MalformedURLException {
        Tweet t = new Tweet();
        t.setText("Just arrived in London in time for #doctorwho http://www.bbc.co.uk/news/uk-18494541");
        t.addHashTag("doctorwho");
        t.addUrl(new URL("http://www.bbc.co.uk/news/uk-18494541"));
        t.setUrl(new URL("http://twitter.com/dpalmisano/statuses/23423"));
        return t;
    }

    private Tweet getSimpleTweet() throws MalformedURLException {
        Tweet t = new Tweet();
        t.setText("Just arrived in London!");
        t.setUrl(new URL("http://twitter.com/dpalmisano/statuses/23423"));
        return t;
    }


    private Tweet getTweetWithHashTag() throws MalformedURLException {
        Tweet t = new Tweet();
        t.setText("Just arrived in London in time for #doctorwho");
        t.addHashTag("doctorwho");
        t.setUrl(new URL("http://twitter.com/dpalmisano/statuses/23423"));
        return t;
    }
}
