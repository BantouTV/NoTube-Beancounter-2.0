package io.beancounter.usermanager.grabber;

import io.beancounter.commons.model.User;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.Context;
import io.beancounter.commons.model.activity.ResolvedActivity;
import io.beancounter.commons.model.activity.Tweet;
import io.beancounter.commons.model.activity.Verb;
import io.beancounter.commons.model.auth.OAuthAuth;
import org.joda.time.DateTime;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;
import org.testng.IObjectFactory;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import twitter4j.HashtagEntity;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.URLEntity;
import twitter4j.auth.AccessToken;

import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Alex Cowell
 */
@PrepareForTest({ TwitterGrabber.class, TwitterFactory.class })
public class TwitterGrabberTest {

    private static final String ACCESS_TOKEN = "7588892-kagSNqWge8gB1WwE3plnFsJHAZVfxWD7Vb57p0b4";
    private static final String ACCESS_TOKEN_SECRET = "PbKfYqSryyeKDWz4ebtY3o5ogNLG11WJuZBc9fQrQo";

    private ActivityGrabber grabber;
    private Twitter twitter;

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionIfLimitIsLessThanOne() throws Exception {
        grabber = new TwitterGrabber(createUser(), "twitter-user-id", 0);
    }

    @Test
    public void shouldReturnOneActivityWhenLimitIsOneAndUserHasOneTweet() throws Exception {
        int limit = 1;
        initMocks(1, limit);

        grabber = new TwitterGrabber(createUser(), "twitter-user-id", limit);
        List<ResolvedActivity> activities = grabber.grab();

        assertNotNull(activities);
        assertEquals(activities.size(), 1);
    }

    @Test
    public void shouldReturnEmptyListWhenLimitIsOneButUserHasNoTweets() throws Exception {
        int limit = 1;
        initMocks(0, limit);

        grabber = new TwitterGrabber(createUser(), "twitter-user-id", limit);
        List<ResolvedActivity> activities = grabber.grab();

        assertNotNull(activities);
        assertTrue(activities.isEmpty());
    }

    @Test
    public void shouldReturnOneActivityWhenLimitIsOneButUserHasManyTweets() throws Exception {
        int limit = 1;
        initMocks(5, limit);

        grabber = new TwitterGrabber(createUser(), "twitter-user-id", limit);
        List<ResolvedActivity> activities = grabber.grab();

        assertNotNull(activities);
        assertEquals(activities.size(), 1);
    }

    @Test
    public void shouldUseCorrectTwitterOAuthCredentialsForUser() throws Exception {
        int limit = 1;
        initMocks(1, limit);

        grabber = new TwitterGrabber(createUser(), "twitter-user-id", limit);
        List<ResolvedActivity> activities = grabber.grab();

        assertNotNull(activities);
        verify(twitter).setOAuthAccessToken(new AccessToken(ACCESS_TOKEN, ACCESS_TOKEN_SECRET));
    }

    @Test
    public void shouldCorrectlyConvertTweetsToResolvedActivities() throws Exception {
        User user = createUser();
        int limit = 1;
        initMocks(1, limit);

        grabber = new TwitterGrabber(user, "twitter-user-id", limit);
        List<ResolvedActivity> activities = grabber.grab();

        assertNotNull(activities);
        assertEquals(activities.size(), 1);

        ResolvedActivity resolvedActivity = activities.get(0);
        assertNotNull(resolvedActivity);
        assertEquals(resolvedActivity.getUserId(), user.getId());
        assertEquals(resolvedActivity.getUser(), user);

        Activity activity = resolvedActivity.getActivity();
        assertNotNull(activity);
        assertNotNull(activity.getId());
        assertEquals(activity.getVerb(), Verb.TWEET);

        Tweet tweet = (Tweet) activity.getObject();
        assertNotNull(tweet);
        assertEquals(tweet.getUrl().toURI(), URI.create("http://twitter.com/test-user/status/161493232834624913"));
        assertEquals(tweet.getText(), "This is a test tweet!");
        Set<String> hashTags = tweet.getHashTags();
        assertEquals(hashTags.size(), 2);
        assertTrue(hashTags.contains("hashtag1"));
        assertTrue(hashTags.contains("hashtag2"));
        List<URL> tweetUrls = tweet.getUrls();
        assertEquals(tweetUrls.size(), 1);
        assertEquals(tweetUrls.get(0).toURI(), URI.create("http://www.example.com/page/1"));

        Context context = activity.getContext();
        assertNotNull(context);
        assertEquals(context.getService(), "twitter");
        assertEquals(context.getUsername(), "twitter-user-id");
        assertEquals(context.getDate(), new DateTime("2012-10-19T21:13:37+0000"));
    }

    private User createUser() {
        User user = new User("Test", "User", "test-user", "password");
        user.addService("twitter", new OAuthAuth(ACCESS_TOKEN, ACCESS_TOKEN_SECRET));
        return user;
    }

    private Status mockStatus() throws Exception {
        DateTime createdAt = new DateTime("2012-10-19T21:13:37+0000");

        twitter4j.User twitterUser = mock(twitter4j.User.class);
        when(twitterUser.getName()).thenReturn("test-user");

        int numHashTags = 2;
        HashtagEntity[] hashTags = new HashtagEntity[numHashTags];
        for (int i = 0; i < numHashTags; i++) {
            HashtagEntity hashtagEntity = mock(HashtagEntity.class);
            when(hashtagEntity.getText()).thenReturn("hashtag" + (i + 1));
            hashTags[i] = hashtagEntity;
        }

        URLEntity urlEntity = mock(URLEntity.class);
        when(urlEntity.getExpandedURL()).thenReturn(new URL("http://www.example.com/page/1"));

        Status status = mock(Status.class);
        when(status.getId()).thenReturn(161493232834624913L);
        when(status.getUser()).thenReturn(twitterUser);
        when(status.getText()).thenReturn("This is a test tweet!");
        when(status.getHashtagEntities()).thenReturn(hashTags);
        when(status.getURLEntities()).thenReturn(new URLEntity[] { urlEntity });
        when(status.getCreatedAt()).thenReturn(createdAt.toDate());

        return status;
    }

    @SuppressWarnings("unchecked")
    private void initMocks(int numStatuses, int limit) throws Exception {
        ResponseList<Status> statuses = mock(ResponseList.class);
        Iterator<Status> statusIterator = mock(Iterator.class);

        if (numStatuses > 0) {
            final int maxStatuses = (numStatuses < limit) ? numStatuses : limit;
            Boolean[] hasNextValues = new Boolean[maxStatuses];
            Status[] nextValues = new Status[maxStatuses];
            for (int i = 0; i < maxStatuses - 1; i++) {
                hasNextValues[i] = true;
                nextValues[i] = mockStatus();
            }
            hasNextValues[maxStatuses - 1] = false;
            when(statusIterator.hasNext()).thenReturn(true, hasNextValues);
            Status firstStatus = mockStatus();
            when(statusIterator.next()).thenReturn(firstStatus, nextValues);
        } else {
            when(statusIterator.hasNext()).thenReturn(false);
        }

        when(statuses.iterator()).thenReturn(statusIterator);

        twitter = mock(Twitter.class);
        TwitterFactory twitterFactory = mock(TwitterFactory.class);
        when(twitterFactory.getInstance()).thenReturn(twitter);
        when(twitter.getUserTimeline("", new Paging(1, limit))).thenReturn(statuses);
        Whitebox.setInternalState(TwitterGrabber.class, twitterFactory);
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }
}
