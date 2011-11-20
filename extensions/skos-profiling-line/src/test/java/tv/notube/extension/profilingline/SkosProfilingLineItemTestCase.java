package tv.notube.extension.profilingline;

import org.joda.time.DateTime;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.model.UserActivities;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.Context;
import tv.notube.commons.model.activity.Song;
import tv.notube.commons.model.activity.Tweet;
import tv.notube.commons.model.activity.Verb;
import tv.notube.profiler.line.ProfilingLineItem;
import tv.notube.profiler.line.ProfilingLineItemException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class SkosProfilingLineItemTestCase {

    private ProfilingLineItem initItem;

    private ProfilingLineItem textItem;

    private ProfilingLineItem mbItem;

    private ProfilingLineItem skos;

    private ProfilingLineItem weight;

    private ProfilingLineItem types;

    @BeforeTest
    public void setUp() {
        initItem = new InitProfilingLineItem("init", "prepares the objects");
        textItem = new TwitterLinkerProfilingLineItem(
                "twitter-linker",
                "it provides dbpedia URLs from Tweets"
        );
        mbItem = new MusicBrainzLinkerProfilingLineItem(
                "music-brainz", "links to dbpedia resolving mbrainz ids"
        );
        skos = new SkosProfilingLineItem("skos", "resolving SKOS subjects");
        weight = new WeightingProfilingLineItem("weight", "weighting interests");
        types = new TypingProfilingLineItem("types", "typing interests");

        types.setNextProfilingLineItem(new TestDumpProfilingLineItem(
                "test",
                "this just dumps")
        );
        weight.setNextProfilingLineItem(types);
        skos.setNextProfilingLineItem(weight);
        mbItem.setNextProfilingLineItem(skos);
        textItem.setNextProfilingLineItem(mbItem);
        initItem.setNextProfilingLineItem(textItem);
    }

    @Test
    public void testSimple() throws MalformedURLException,
            ProfilingLineItemException {
        String username = "dpalmisano";

        Activity a = new Activity();
        a.setVerb(Verb.TWEET);
        Tweet t = new Tweet();
        t.setUrl(new URL("http://twitter.com/dpalmisano/status/4637642"));
        t.setText("heading to the airport - leaving Rome for a couple of weeks");
        a.setObject(t);
        Context context = new Context();
        context.setDate(new DateTime());
        context.setService(new URL("http://twitter.com"));
        a.setContext(context);

        Activity a2 = new Activity();
        a2.setVerb(Verb.LISTEN);
        Song s = new Song();
        s.setUrl(new URL("http://last.fm/song/4637642"));
        s.setMbid("244afcb7-fa9a-49b1-9aa6-0149512d1c52");
        a2.setObject(s);
        Context context2 = new Context();
        context2.setDate(new DateTime());
        context2.setService(new URL("http://last.fm"));
        a2.setContext(context2);

        Activity a3 = new Activity();
        a3.setVerb(Verb.TWEET);
        Tweet tweet2 = new Tweet();
        tweet2.setUrl(new URL("http://twitter.com/dpalmisano/status/66352"));
        tweet2.setText("Listening to the Thrice best album ever");
        a3.setObject(tweet2);
        Context context3 = new Context();
        context3.setDate(new DateTime());
        context3.setService(new URL("http://twitter.com"));
        a3.setContext(context3);

        Activity a4 = new Activity();
        a4.setVerb(Verb.TWEET);
        Tweet tweet3 = new Tweet();
        tweet3.setUrl(new URL("http://twitter.com/dpalmisano/status/11736"));
        tweet3.setText("Heading to Thrice gig tonight in London");
        a4.setObject(tweet2);
        Context context4 = new Context();
        context4.setDate(new DateTime());
        context4.setService(new URL("http://twitter.com"));
        a4.setContext(context4);

        List<Activity> activities = new ArrayList<Activity>();
        activities.add(a);
        activities.add(a2);
        activities.add(a3);
        activities.add(a4);

        UserActivities userActivities =
                new UserActivities(username, activities);

        initItem.execute(userActivities);
    }

    @Test
    public void testRichTweet() throws MalformedURLException, ProfilingLineItemException {
        Tweet tweet = new Tweet();
        tweet.addHashTag("raiperunanotte");
        tweet.addUrl(new URL("http://www.axessjournalism" +
                ".com/blog/2010/3/27/on-rai-per-una-notte"));
        tweet.setText("just watched an amazing #raiperunanotte episode. " +
                "Berlusconi go home!");

        Activity a = new Activity();
        a.setVerb(Verb.TWEET);
        a.setObject(tweet);

        Context context = new Context();
        context.setDate(new DateTime());
        context.setService(new URL("http://twitter.com"));
        a.setContext(context);
        List<Activity> activities = new ArrayList<Activity>();
        activities.add(a);
        String username = "dpalmisano";

        UserActivities userActivities = new UserActivities(username, activities);
        initItem.execute(userActivities);
    }

}
