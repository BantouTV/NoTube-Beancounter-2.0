package tv.notube.resolver.tv.notube.filter.model.pattern;

import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.Test;
import tv.notube.commons.model.User;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.Context;
import tv.notube.commons.model.activity.ResolvedActivity;
import tv.notube.commons.model.activity.Verb;
import tv.notube.commons.model.activity.rai.Comment;
import tv.notube.commons.model.activity.rai.TVEvent;
import tv.notube.commons.model.auth.OAuthAuth;
import tv.notube.filter.manager.FilterManager;
import tv.notube.filter.manager.FilterManagerException;
import tv.notube.filter.manager.InMemoryFilterManager;
import tv.notube.filter.model.pattern.*;
import tv.notube.filter.model.pattern.rai.CommentPattern;
import tv.notube.filter.model.pattern.rai.TVEventPattern;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * Reference test case for {@link Pattern}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class PatternTestCase {

    @Test
    public void testVerbPattern() {
        VerbPattern vp = new VerbPattern(Verb.LIKE);
        Assert.assertFalse(vp.matches(Verb.TWEET));
        Assert.assertFalse(vp.matches(Verb.SHARE));
        Assert.assertTrue(vp.matches(Verb.LIKE));
        vp = VerbPattern.ANY;
        Assert.assertTrue(vp.matches(Verb.TWEET));
        Assert.assertTrue(vp.matches(Verb.SHARE));
        Assert.assertTrue(vp.matches(Verb.LIKE));
    }

    @Test
    public void testDateTimePattern() {
        DateTimePattern dtp = new DateTimePattern(
                DateTime.now().minusDays(1),
                DateTimePattern.Bool.AFTER
        );
        // is today after yesterday?
        Assert.assertTrue(dtp.matches(DateTime.now()));
        // is 2 days ago after yesterday?
        Assert.assertFalse(dtp.matches(DateTime.now().minusDays(2)));
        dtp = DateTimePattern.ANY;
        // these should pass, any is a non filter
        Assert.assertTrue(dtp.matches(DateTime.now()));
        Assert.assertTrue(dtp.matches(DateTime.now().minusDays(2)));
        Assert.assertTrue(dtp.matches(DateTime.now().plusDays(200)));
    }

    @Test
    public void testUrlPattern() throws MalformedURLException {
        URLPattern up = new URLPattern("http://test.com");
        URL matching = new URL("http://test.com");
        Assert.assertTrue(up.matches(matching));

        URL unmatching = new URL("http://fake.com/");
        Assert.assertFalse(up.matches(unmatching));

        up = URLPattern.ANY;
        // everything should match
        Assert.assertTrue(up.matches(matching));
        Assert.assertTrue(up.matches(unmatching));
    }

    @Test
    public void testUUIDPattern() {
        UUID matching = UUID.randomUUID();
        UUIDPattern up = new UUIDPattern(
                matching
        );
        Assert.assertTrue(up.matches(matching));
        UUID unmatching = UUID.randomUUID();
        Assert.assertFalse(up.matches(unmatching));

        up = UUIDPattern.ANY;
        // everything matches
        Assert.assertTrue(up.matches(matching));
        Assert.assertTrue(up.matches(unmatching));
    }

    @Test
    public void testStringPattern() {
        StringPattern sp = new StringPattern("test-string");
        Assert.assertTrue(sp.matches("test-string"));
        Assert.assertFalse(sp.matches("test-string-false"));
        sp = StringPattern.ANY;
        Assert.assertTrue(sp.matches("test-string"));
        Assert.assertTrue(sp.matches("test-string-false"));
    }

    @Test
    public void testContextPattern() {
        // a pattern matching only stuff coming from twitter
        // happened yesterday
        ContextPattern cp = new ContextPattern(
                new DateTimePattern(DateTime.now().minusDays(1), DateTimePattern.Bool.AFTER),
                new StringPattern("twitter"),
                StringPattern.ANY,
                StringPattern.ANY
        );

        // matching context
        Context matching = new Context(DateTime.now());
        matching.setService("twitter");
        matching.setUsername("a-username");
        matching.setMood("a-mood");
        Assert.assertTrue(cp.matches(matching));

        // unmatching context 1
        Context unmatching1 = new Context(DateTime.now().minusDays(2));
        unmatching1.setService("twitter");
        unmatching1.setUsername("a-username");
        unmatching1.setMood("a-mood");
        Assert.assertFalse(cp.matches(unmatching1));

        // unmatching context 2
        Context unmatching2 = new Context(DateTime.now());
        unmatching2.setService("facebook");
        unmatching2.setUsername("a-username");
        unmatching2.setMood("a-mood");
        Assert.assertFalse(cp.matches(unmatching2));

        cp = ContextPattern.ANY;
        // matches anything
        Assert.assertTrue(cp.matches(matching));
        Assert.assertTrue(cp.matches(unmatching1));
        Assert.assertTrue(cp.matches(unmatching2));
    }

    /**
     * Just as a reference for <i>rai.tv</i> stuff.
     */
    @Test
    public void testTVEventPattern() throws MalformedURLException {
        UUID matchingId = UUID.randomUUID();
        TVEventPattern tvp = new TVEventPattern(
                new UUIDPattern(matchingId),
                URLPattern.ANY
        );

        TVEvent matching = new TVEvent(
                matchingId,
                "test-tv-event",
                "a fake description"
        );
        Assert.assertTrue(tvp.matches(matching));

        TVEvent unmatching = new TVEvent(
                UUID.randomUUID(),
                "test-tv-event",
                "a fake description"
        );
        Assert.assertFalse(tvp.matches(unmatching));

        tvp = TVEventPattern.ANY;
        // now everything should pass
        Assert.assertTrue(tvp.matches(matching));
        Assert.assertTrue(tvp.matches(unmatching));
    }

    @Test
    public void testCommentPattern() throws FilterManagerException, IOException {
        Activity activity = new Activity();
        activity.setId(UUID.fromString("08912993-9fc5-4038-aba0-95c6495540d6"));
        activity.setVerb(Verb.SHARE);
        Comment comment = new Comment();
        comment.setText("thisisatextofacomment");
        comment.setInReplyTo(UUID.fromString("17efdae2-c803-4411-aac9-f6185bdf13de"));
        comment.setOnEvent("ContentSet-07499e81-1058-4ea0-90f7-77f0fc17eade");
        Context context = new Context();
        context.setDate(new DateTime().minusDays(30));
        context.setService("rai.tv");
        context.setUsername("dpalmisano");
        activity.setObject(comment);
        activity.setContext(context);

        User user = new User();
        user.setName("test-name");
        user.setSurname("test-surname");
        user.setPassword("test-pwd");
        user.setUsername("test-username");
        user.addService("test-service", new OAuthAuth("se", "sc"));

        final ResolvedActivity actual = new ResolvedActivity(
                UUID.fromString("4a832181-b691-4f50-ac7e-86937aa3fd03"),
                activity,
                user
        );

        CommentPattern commentPattern = new CommentPattern();
        commentPattern.setOnEventPattern(
                new StringPattern("ContentSet-07499e81-1058-4ea0-90f7-77f0fc17eade")
        );

        ActivityPattern activityPattern = new ActivityPattern();
        activityPattern.setUserId(UUIDPattern.ANY);
        activityPattern.setVerb(VerbPattern.ANY);
        activityPattern.setContext(ContextPattern.ANY);
        activityPattern.setObject(commentPattern);

        Assert.assertTrue(commentPattern.matches(comment));
        FilterManager fm = new InMemoryFilterManager();
        fm.register("test-filter", "test filter", "queue", activityPattern);
        boolean b = fm.get("test-filter").getActivityPattern().matches(actual);
        Assert.assertTrue(b);

        // let's change the onEvent UUID
        commentPattern = new CommentPattern();
        commentPattern.setOnEventPattern(
                new StringPattern("ContentSet-17499e81-1058-4ea0-90f7-77f0fc17eade")
        );
        activityPattern.setObject(commentPattern);
        fm.delete("test-filter");
        fm.register("test-filter", "test filter", "queue", activityPattern);
        b = fm.get("test-filter").getActivityPattern().matches(actual);
        Assert.assertFalse(b);
    }

}
