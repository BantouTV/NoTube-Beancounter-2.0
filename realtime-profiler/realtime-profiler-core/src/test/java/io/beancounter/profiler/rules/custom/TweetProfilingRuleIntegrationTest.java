package io.beancounter.profiler.rules.custom;

import io.beancounter.commons.cogito.CogitoNLPEngineImpl;
import io.beancounter.commons.model.Interest;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import io.beancounter.commons.model.activity.Tweet;
import io.beancounter.profiler.rules.ObjectProfilingRule;
import io.beancounter.profiler.rules.ProfilingRuleException;

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
public class TweetProfilingRuleIntegrationTest {

    private static final String endpoint = "http://test.expertsystem.it/IPTC_ITA/EssexWS.asmx/ESSEXIndexdata";

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
                new CogitoNLPEngineImpl(endpoint),
                null
        );
        rule.run(properties);
        Collection<Interest> actual = rule.getResult();
        Assert.assertEquals(actual.size(), 1);
        Assert.assertTrue(actual.contains(new Interest("Londra", new URI("http://dati.rai.tv/entity/12631236"))));
    }

    @Test
    public void testTextAndAnHashTag() throws ProfilingRuleException, URISyntaxException, MalformedURLException {
        rule = new TweetProfilingRule(
                getTweetWithHashTag(),
                new CogitoNLPEngineImpl(endpoint),
                null
        );
        rule.run(properties);
        Collection<Interest> actual = rule.getResult();
        Assert.assertEquals(actual.size(), 5);
        Assert.assertTrue(actual.contains(new Interest("Londra", new URI("http://dati.rai.tv/entity/12631236"))));
        Assert.assertTrue(actual.contains(new Interest("science fiction", new URI("http://dati.rai.tv/category/science+fiction"))));
        Assert.assertTrue(actual.contains(new Interest("BBC", new URI("http://dati.rai.tv/entity/278188"))));
    }

    @Test
    public void testTextAnHashTagAndAnURL() throws ProfilingRuleException,
            URISyntaxException, MalformedURLException {
        rule = new TweetProfilingRule(
                getTweetWithHashTagAndUrl(),
                new CogitoNLPEngineImpl(endpoint),
                null
        );
        rule.run(properties);
        Collection<Interest> actual = rule.getResult();
        Assert.assertEquals(actual.size(), 11);
        Assert.assertTrue(actual.contains(new Interest("Londra", new URI("http://dati.rai.tv/entity/12631236"))));
        Assert.assertTrue(actual.contains(new Interest("science fiction", new URI("http://dati.rai.tv/category/science+fiction"))));
        Assert.assertTrue(actual.contains(new Interest("BBC", new URI("http://dati.rai.tv/entity/278188"))));
        Assert.assertTrue(actual.contains(new Interest("Cardiff", new URI("http://dati.rai.tv/entity/12641124"))));
        Assert.assertTrue(actual.contains(new Interest("Barnsley", new URI("http://dati.rai.tv/entity/12643544"))));
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
