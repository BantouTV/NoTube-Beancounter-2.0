package io.beancounter.usermanager.grabber;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.restfb.DefaultJsonMapper;
import com.restfb.FacebookClient;
import com.restfb.types.Post;
import io.beancounter.commons.model.User;
import io.beancounter.commons.model.activity.*;
import io.beancounter.commons.model.activity.Object;
import io.beancounter.commons.model.activity.facebook.Like;
import io.beancounter.commons.model.auth.OAuthAuth;
import io.beancounter.listener.facebook.core.FacebookUtils;
import io.beancounter.listener.facebook.core.converter.custom.FacebookLikeConverter;
import io.beancounter.listener.facebook.core.model.FacebookData;
import org.joda.time.DateTime;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.IObjectFactory;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@PrepareForTest({ FacebookUtils.class, FacebookGrabber.class })
public class FacebookGrabberTest {

    private ActivityGrabber grabber;

    @Test(enabled = false)
    public void shouldCreateFacebookClientWithCorrectOAuthCredentials() throws Exception {
        // TODO: There is no nice way to check the OAuth access token being used
        // by the FacebookClient with the current implementation. See TODO in
        // FacebookGrabber for another idea.
        fail();
        User user = createUser();
        ImmutableMap<String, Integer> limits = ImmutableMap.of(
                FacebookGrabber.SHARES, 1,
                FacebookGrabber.LIKES, 1
        );
        List<Post> retrievedShares = listOfShares(1);
        List<FacebookData> retrievedLikes = listOfLikes(1);

        //ArgumentCaptor<FacebookClient> clientCaptor = ArgumentCaptor.forClass(FacebookClient.class);
        mockStatic(FacebookUtils.class);
        when(FacebookUtils.fetch(eq(Post.class), any(FacebookClient.class), eq("feed"), eq(1)))
                .thenReturn(retrievedShares);
        when(FacebookUtils.fetch(eq(FacebookData.class), any(FacebookClient.class), eq("likes"), eq(1)))
                .thenReturn(retrievedLikes);

        grabber = new FacebookGrabber(user, "facebook-user-id", limits);
        List<ResolvedActivity> activities = grabber.grab();

        //FacebookClient client = clientCaptor.getValue();
    }

    @Test
    public void shouldReturnEmptyListWhenLimitsAreZero() throws Exception {
        ImmutableMap<String, Integer> limits = ImmutableMap.of(
                FacebookGrabber.SHARES, 0,
                FacebookGrabber.LIKES, 0
        );
        List<Post> retrievedShares = listOfShares(0);
        List<FacebookData> retrievedLikes = listOfLikes(0);

        mockStatic(FacebookUtils.class);
        when(FacebookUtils.fetch(eq(Post.class), any(FacebookClient.class), eq("feed"), eq(0)))
                .thenReturn(retrievedShares);
        when(FacebookUtils.fetch(eq(FacebookData.class), any(FacebookClient.class), eq("likes"), eq(0)))
                .thenReturn(retrievedLikes);

        grabber = new FacebookGrabber(createUser(), "facebook-user-id", limits);
        List<ResolvedActivity> activities = grabber.grab();

        assertNotNull(activities);
        assertTrue(activities.isEmpty());
    }

    @Test
    public void shouldReturnNonEmptyListWhenLimitsAreAboveZeroAndUserHasActivities() throws Exception {
        ImmutableMap<String, Integer> limits = ImmutableMap.of(
                FacebookGrabber.SHARES, 1,
                FacebookGrabber.LIKES, 1
        );
        List<Post> retrievedShares = listOfShares(1);
        List<FacebookData> retrievedLikes = listOfLikes(1);

        initMocks(1, retrievedShares, 1, retrievedLikes);

        grabber = new FacebookGrabber(createUser(), "facebook-user-id", limits);
        List<ResolvedActivity> activities = grabber.grab();

        assertNotNull(activities);
        assertFalse(activities.isEmpty());
    }

    @Test
    public void shouldReturnFiveActivitiesWhenUserHasFiveSharesAndShareLimitIsFive() throws Exception {
        ImmutableMap<String, Integer> limits = ImmutableMap.of(
                FacebookGrabber.SHARES, 5,
                FacebookGrabber.LIKES, 0
        );
        List<Post> retrievedShares = listOfShares(5);
        List<FacebookData> retrievedLikes = listOfLikes(0);

        initMocks(5, retrievedShares, 0, retrievedLikes);

        grabber = new FacebookGrabber(createUser(), "facebook-user-id", limits);
        List<ResolvedActivity> activities = grabber.grab();

        assertNotNull(activities);
        assertEquals(activities.size(), 5);
    }

    @Test
    public void shouldReturnFiveActivitiesWhenUserHasFiveLikesAndLikeLimitIsFive() throws Exception {
        ImmutableMap<String, Integer> limits = ImmutableMap.of(
                FacebookGrabber.SHARES, 0,
                FacebookGrabber.LIKES, 5
        );
        List<Post> retrievedShares = listOfShares(0);
        List<FacebookData> retrievedLikes = listOfLikes(5);

        initMocks(0, retrievedShares, 5, retrievedLikes);

        grabber = new FacebookGrabber(createUser(), "facebook-user-id", limits);
        List<ResolvedActivity> activities = grabber.grab();

        assertNotNull(activities);
        assertEquals(activities.size(), 5);
    }

    @Test
    public void shouldReturnTenActivitiesWhenUserHasFiveSharesAndFiveLikesAndLimitsAreFive() throws Exception {
        ImmutableMap<String, Integer> limits = ImmutableMap.of(
                FacebookGrabber.SHARES, 5,
                FacebookGrabber.LIKES, 5
        );
        List<Post> retrievedShares = listOfShares(5);
        List<FacebookData> retrievedLikes = listOfLikes(5);

        initMocks(5, retrievedShares, 5, retrievedLikes);

        grabber = new FacebookGrabber(createUser(), "facebook-user-id", limits);
        List<ResolvedActivity> activities = grabber.grab();

        assertNotNull(activities);
        assertEquals(activities.size(), 10);
    }

    @Test
    public void shouldIgnoreLikesWhenTheLikeLimitIsNotSet() throws Exception {
        ImmutableMap<String, Integer> limits = ImmutableMap.of(
                FacebookGrabber.SHARES, 5
                // Like limit not set
        );
        List<Post> retrievedShares = listOfShares(5);

        initMocks(5, retrievedShares, 0, listOfLikes(0));

        grabber = new FacebookGrabber(createUser(), "facebook-user-id", limits);
        List<ResolvedActivity> activities = grabber.grab();

        assertNotNull(activities);
        assertEquals(activities.size(), 5);
        verifyStatic(never());
        FacebookUtils.fetch(eq(FacebookData.class), any(FacebookClient.class), eq("likes"), anyInt());
    }

    @Test
    public void shouldIgnoreSharesWhenTheShareLimitIsNotSet() throws Exception {
        ImmutableMap<String, Integer> limits = ImmutableMap.of(
                // Share limit not set
                FacebookGrabber.LIKES, 5
        );
        List<FacebookData> retrievedLikes = listOfLikes(5);

        initMocks(0, listOfShares(0), 5, retrievedLikes);

        grabber = new FacebookGrabber(createUser(), "facebook-user-id", limits);
        List<ResolvedActivity> activities = grabber.grab();

        assertNotNull(activities);
        assertEquals(activities.size(), 5);
        verifyStatic(never());
        FacebookUtils.fetch(eq(Post.class), any(FacebookClient.class), eq("feed"), anyInt());
    }

    @Test
    public void shouldIgnoreLikesWhenTheLikeLimitIsNegative() throws Exception {
        ImmutableMap<String, Integer> limits = ImmutableMap.of(
                FacebookGrabber.SHARES, 5,
                FacebookGrabber.LIKES, -1
        );
        List<Post> retrievedShares = listOfShares(5);

        initMocks(5, retrievedShares, -1, listOfLikes(0));

        grabber = new FacebookGrabber(createUser(), "facebook-user-id", limits);
        List<ResolvedActivity> activities = grabber.grab();

        assertNotNull(activities);
        assertEquals(activities.size(), 5);
        verifyStatic(never());
        FacebookUtils.fetch(eq(FacebookData.class), any(FacebookClient.class), eq("likes"), anyInt());
    }

    @Test
    public void shouldIgnoreSharesWhenTheShareLimitIsNegative() throws Exception {
        ImmutableMap<String, Integer> limits = ImmutableMap.of(
                FacebookGrabber.SHARES, -1,
                FacebookGrabber.LIKES, 5
        );
        List<FacebookData> retrievedLikes = listOfLikes(5);

        initMocks(-1, listOfShares(0), 5, retrievedLikes);

        grabber = new FacebookGrabber(createUser(), "facebook-user-id", limits);
        List<ResolvedActivity> activities = grabber.grab();

        assertNotNull(activities);
        assertEquals(activities.size(), 5);
        verifyStatic(never());
        FacebookUtils.fetch(eq(Post.class), any(FacebookClient.class), eq("feed"), anyInt());
    }

    @Test
    public void shouldCorrectlyConvertSharesToResolvedActivities() throws Exception {
        User user = createUser();
        ImmutableMap<String, Integer> limits = ImmutableMap.of(
                FacebookGrabber.SHARES, 1
        );
        List<Post> retrievedShares = Lists.newArrayList(createShare());

        mockStatic(FacebookUtils.class);
        when(FacebookUtils.fetch(eq(Post.class), any(FacebookClient.class), eq("feed"), eq(1)))
                .thenReturn(retrievedShares);

        grabber = new FacebookGrabber(user, "facebook-user-id", limits);
        List<ResolvedActivity> activities = grabber.grab();

        assertNotNull(activities);
        assertEquals(activities.size(), 1);

        ResolvedActivity resolvedActivity = activities.get(0);
        assertEquals(resolvedActivity.getUserId(), user.getId());
        assertEquals(resolvedActivity.getUser(), user);

        Activity activity = resolvedActivity.getActivity();
        assertNotNull(activity);
        assertNotNull(activity.getId());
        assertEquals(activity.getVerb(), Verb.SHARE);

        Object object = activity.getObject();
        assertNotNull(object);
        assertEquals(object.getName(), "Scientists make incredible discovery in Uranus - Example.com");
        assertEquals(object.getDescription(), "Description for the shared item.");
        assertEquals(object.getUrl().toURI(), URI.create("http://www.example.com/news/world/Amazing-Discovery-Science/15-10-2012"));

        Context context = activity.getContext();
        assertNotNull(context);
        assertEquals(context.getService(), "facebook");
        assertEquals(context.getUsername(), "facebook-user-id");
        assertEquals(context.getDate(), new DateTime("2012-10-19T21:34:31+0000"));
    }

    @Test
    public void shouldCorrectlyConvertLikesToResolvedActivities() throws Exception {
        User user = createUser();
        ImmutableMap<String, Integer> limits = ImmutableMap.of(
                FacebookGrabber.LIKES, 1
        );
        List<FacebookData> retrievedLikes = Lists.newArrayList(createLike());

        mockStatic(FacebookUtils.class);
        when(FacebookUtils.fetch(eq(FacebookData.class), any(FacebookClient.class), eq("likes"), eq(1)))
                .thenReturn(retrievedLikes);

        grabber = new FacebookGrabber(user, "facebook-user-id", limits);
        List<ResolvedActivity> activities = grabber.grab();

        assertNotNull(activities);
        assertEquals(activities.size(), 1);

        ResolvedActivity resolvedActivity = activities.get(0);
        assertEquals(resolvedActivity.getUserId(), user.getId());
        assertEquals(resolvedActivity.getUser(), user);

        Activity activity = resolvedActivity.getActivity();
        assertNotNull(activity);
        assertNotNull(activity.getId());
        assertEquals(activity.getVerb(), Verb.LIKE);

        Like object = (Like) activity.getObject();
        assertNotNull(object);
        assertEquals(object.getName(), "Beancounter 2.0: The Movie (2012)");
        assertEquals(object.getUrl().toURI(), URI.create("http://www.facebook.com/121212121212121"));
        assertEquals(object.getCategories().size(), 1);
        assertEquals(object.getCategories().iterator().next(), "Movie");

        Context context = activity.getContext();
        assertNotNull(context);
        assertEquals(context.getService(), "facebook");
        assertEquals(context.getUsername(), "facebook-user-id");
        assertEquals(context.getDate(), new DateTime("2012-07-24T14:38:36+0000"));
    }

    @Test
    public void shouldContinueWhenAConverterExceptionOccurs() throws Exception {
        ImmutableMap<String, Integer> limits = ImmutableMap.of(
                FacebookGrabber.SHARES, 2,
                FacebookGrabber.LIKES, 2
        );
        List<Post> retrievedShares = Lists.newArrayList(new Post(), createShare());
        List<FacebookData> retrievedLikes = listOfLikes(2);

        initMocks(2, retrievedShares, 2, retrievedLikes);

        grabber = new FacebookGrabber(createUser(), "facebook-user-id", limits);
        List<ResolvedActivity> activities = grabber.grab();

        assertNotNull(activities);
        assertEquals(activities.size(), 3);
    }

    private User createUser() {
        User user = new User("Test", "User", "test-user", "password");
        user.addService("facebook", new OAuthAuth("facebook-oauth", null));
        return user;
    }

    private void initMocks(int shareLimit, List<Post> shares, int likeLimit, List<FacebookData> likes) throws Exception {
        // Prevents the FacebookLikeConverter from making slow HTTP calls to
        // Facebook's API.
        FacebookLikeConverter likeConverter = mock(FacebookLikeConverter.class);
        whenNew(FacebookLikeConverter.class).withNoArguments().thenReturn(likeConverter);

        mockStatic(FacebookUtils.class);
        when(FacebookUtils.fetch(eq(Post.class), any(FacebookClient.class), eq("feed"), eq(shareLimit)))
                .thenReturn(shares);
        when(FacebookUtils.fetch(eq(FacebookData.class), any(FacebookClient.class), eq("likes"), eq(likeLimit)))
                .thenReturn(likes);
    }

    private static List<Post> listOfShares(int numShares) {
        List<Post> shares = new ArrayList<Post>();
        for (int i = 0; i < numShares; i++) {
            shares.add(createShare());
        }
        return shares;
    }

    private static List<FacebookData> listOfLikes(int numLikes) {
        List<FacebookData> likes = new ArrayList<FacebookData>();
        for (int i = 0; i < numLikes; i++) {
            likes.add(createLike());
        }
        return likes;
    }

    private static Post createShare() {
        String json = "{" +
                "         \"id\": \"123456789876543_376434979098491\"," +
                "         \"from\": {" +
                "            \"name\": \"Test User\"," +
                "            \"id\": \"123456789876543\"" +
                "         }," +
                "         \"story\": \"Test User shared a link.\"," +
                "         \"link\": \"http://www.example.com/news/world/Amazing-Discovery-Science/15-10-2012\"," +
                "         \"name\": \"Scientists make incredible discovery in Uranus - Example.com\"," +
                "         \"description\": \"Description for the shared item.\"," +
                "         \"caption\": \"www.example.com\"," +
                "         \"icon\": \"http://static.ak.fbcdn.net/rsrc.php/v2/yD/r/aS8ecmYRys0.gif\"," +
                "         \"actions\": [" +
                "            {" +
                "               \"name\": \"Comment\"," +
                "               \"link\": \"http://www.facebook.com/123456789876543/posts/376434979098491\"" +
                "            }," +
                "            {" +
                "               \"name\": \"Like\"," +
                "               \"link\": \"http://www.facebook.com/123456789876543/posts/376434979098491\"" +
                "            }" +
                "         ]," +
                "         \"privacy\": {" +
                "            \"description\": \"Public\"," +
                "            \"value\": \"EVERYONE\"" +
                "         }," +
                "         \"type\": \"link\"," +
                "         \"status_type\": \"shared_story\"," +
                "         \"application\": {" +
                "            \"name\": \"Share_bookmarklet\"," +
                "            \"id\": \"5085647995\"" +
                "         }," +
                "         \"created_time\": \"2012-10-19T21:34:31+0000\"," +
                "         \"updated_time\": \"2012-10-19T21:34:31+0000\"," +
                "         \"comments\": {" +
                "            \"count\": 0" +
                "         }" +
                "      }";

        return new DefaultJsonMapper().toJavaObject(json, Post.class);
    }

    private static FacebookData createLike() {
        String json = "{" +
                "         \"name\": \"Beancounter 2.0: The Movie (2012)\"," +
                "         \"category\": \"Movie\"," +
                "         \"id\": \"121212121212121\"," +
                "         \"created_time\": \"2012-07-24T14:38:36+0000\"" +
                "      }";

        return new DefaultJsonMapper().toJavaObject(json, FacebookData.class);
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }
}
