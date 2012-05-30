package tv.notube.extension.profilingline;

import org.joda.time.DateTime;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.model.UserActivities;
import tv.notube.commons.model.activity.*;
import tv.notube.commons.model.activity.Object;
import tv.notube.profiler.line.ProfilingLineItem;
import tv.notube.profiler.line.ProfilingLineItemException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class DBpediaProfilingLineItemTestCase {

    // private static final String API_KEY = "04490000a72fe7ec5cb3497f14e77f338c86f2fe";

    // private static final String API_KEY = "682289043b579238db5b7cb0aa25b88be3e2ef0e";

    private static final String API_KEY =  "04490000a72fe7ec5cb3497f14e77f338c86f2fe";

    private ProfilingLineItem initItem;

    private ProfilingLineItem textItem;

    private ProfilingLineItem fbItem;

    private ProfilingLineItem mbItem;

    private ProfilingLineItem weight;

    private ProfilingLineItem building;


    @BeforeTest
    public void setUp() {
        initItem = new InitProfilingLineItem("init", "prepares the objects");

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("apikey", API_KEY);
        textItem = new TwitterLinkerProfilingLineItem(
                "twitter-linker",
                "it provides dbpedia URLs from Tweets",
                parameters);

        mbItem = new MusicBrainzLinkerProfilingLineItem(
                "music-brainz", "links to dbpedia resolving mbrainz ids"
        );

        fbItem = new FacebookProfilingLineItem(
                "facebook-linker",
                "it provides links to dbpedia from FB"
        );

        weight = new WeightingProfilingLineItem("weight", "weighting interests");

        building = new ProfileBuildingProfilingLineItem(
                "build",
                "it builds the profile"
        );

        building.setNextProfilingLineItem(new TestDumpProfilingLineItem(
                "test",
                "this just dumps")
        );
        weight.setNextProfilingLineItem(building);
        fbItem.setNextProfilingLineItem(weight);
        mbItem.setNextProfilingLineItem(fbItem);
        textItem.setNextProfilingLineItem(mbItem);
        initItem.setNextProfilingLineItem(textItem);
    }

    @Test(enabled = false)
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

        Activity a5 = new Activity();
        a5.setVerb(Verb.LIKE);
        Object like1 = new Object();
        like1.setName("The Goonies");
        like1.setDescription("Movie");
        like1.setUrl(new URL("https://www.facebook.com/thegooniesmovie"));
        a5.setObject(like1);
        Context context5 = new Context();
        context5.setDate(new DateTime());
        context5.setService(new URL("http://facebook.com"));
        a5.setContext(context5);

        List<Activity> activities = new ArrayList<Activity>();
        activities.add(a);
        activities.add(a2);
        activities.add(a3);
        activities.add(a4);
        activities.add(a5);

        UserActivities userActivities =
                new UserActivities(username, activities);

        initItem.execute(userActivities);
    }

    @Test(enabled = false)
    public void testRichTweet() throws MalformedURLException, ProfilingLineItemException {
        Tweet tweet = new Tweet();
        tweet.addHashTag("raiperunanotte");
        tweet.addUrl(new URL("http://www.axessjournalism.com/blog/2010/3/27/on-rai-per-una-notte"));
        tweet.setText("just watched an amazing #raiperunanotte episode. Berlusconi go home!");

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
