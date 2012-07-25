package tv.notube.activities;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.ActivityBuilder;
import tv.notube.commons.model.activity.Context;
import tv.notube.commons.model.activity.DefaultActivityBuilder;
import tv.notube.commons.model.activity.Song;
import tv.notube.commons.model.activity.Tweet;
import tv.notube.commons.model.activity.Verb;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static tv.notube.activities.ElasticSearchActivityStoreImpl.INDEX_NAME;
import static tv.notube.activities.ElasticSearchActivityStoreImpl.INDEX_TYPE;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 * @author Alex Cowell ( alxcwll@gmail.com )
 */
public class ElasticSearchActivityStoreTest {

    private ActivityStore as;

    private Node node;
    private Client client;
    private final String tweetServiceUrl = "http://twitter.com";
    private final String lastFmServiceUrl = "http://last.fm";

    @BeforeSuite
    public void beforeSuite() throws Exception {
        node = NodeBuilder.nodeBuilder().node();
        client = node.client();

        try {
            // The default index will be created with 5 shards with 1 replica
            // per shard.
            client.admin().indices().create(new CreateIndexRequest(INDEX_NAME)).actionGet();
        } catch (ElasticSearchException indexAlreadyExists) {
            clearIndex();
        }

        // Wait for shards to settle in the idle state before continuing.
        client.admin().cluster().health(new ClusterHealthRequest(INDEX_NAME)
                .waitForYellowStatus()).actionGet();
    }

    @AfterSuite
    public void afterSuite() throws Exception {
        node.close();
        // TODO (mid): Remove the ES data dir
    }

    @BeforeTest
    public void setUp() throws Exception {
        as = ElasticSearchActivityStoreFactory.getInstance().build();
    }

    @AfterTest
    public void tearDown() throws Exception {
        as.shutDown();
        as = null;
    }

    @BeforeMethod
    public void beforeMethod() throws Exception {
        clearIndex();
    }

    @Test
    public void storeASingleTweetForAUser() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime();
        Activity tweetActivity = createTweetActivity(0, userId, dateTime);

        as.store(userId, tweetActivity);

        refreshIndex();

        SearchResponse searchResponse = client.prepareSearch(INDEX_NAME)
                .setQuery(QueryBuilders.matchAllQuery())
                .execute().actionGet();

        SearchHits hits = searchResponse.getHits();
        assertEquals(hits.getTotalHits(), 1);

        SearchHit hit = hits.getAt(0);
        assertHitEqualsTweetActivity(hit, userId, tweetActivity);
    }

    @Test
    public void storeASingleListenForAUser() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime();
        Activity lastFmActivity = createLastFmActivity(0, userId, dateTime);

        as.store(userId, lastFmActivity);

        refreshIndex();

        SearchResponse searchResponse = client.prepareSearch(INDEX_NAME)
                .setQuery(QueryBuilders.matchAllQuery())
                .execute().actionGet();

        SearchHits hits = searchResponse.getHits();
        assertEquals(hits.getTotalHits(), 1);

        SearchHit hit = hits.getAt(0);
        assertHitEqualsLastFmActivity(hit, userId, lastFmActivity);
    }

    @Test
    public void storeMultipleTweetsForAUser() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime();
        int numTweets = 5;

        Collection<Activity> tweetActivities = createTweetActivities(userId, dateTime, numTweets);
        as.store(userId, tweetActivities);

        refreshIndex();

        SearchResponse searchResponse = client.prepareSearch(INDEX_NAME)
                .setQuery(QueryBuilders.matchAllQuery())
                .addSort(INDEX_TYPE + ".activity.context.date", SortOrder.DESC)
                .execute().actionGet();

        SearchHits hits = searchResponse.getHits();
        assertEquals(hits.getTotalHits(), numTweets);

        assertHitsEqualTweetActivities(hits, userId, tweetActivities);
    }

    @Test
    public void storeMultipleListensForAUser() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime();
        int numListens = 5;

        Collection<Activity> lastFmActivities
                = createLastFmActivities(userId, dateTime, numListens);
        as.store(userId, lastFmActivities);

        refreshIndex();

        SearchResponse searchResponse = client.prepareSearch(INDEX_NAME)
                .setQuery(QueryBuilders.matchAllQuery())
                .addSort(INDEX_TYPE + ".activity.context.date", SortOrder.DESC)
                .execute().actionGet();

        SearchHits hits = searchResponse.getHits();
        assertEquals(hits.getTotalHits(), numListens);

        assertHitsEqualLastFmActivities(hits, userId, lastFmActivities);
    }

    @Test
    public void getLatestTweetByAUserGivenOneUserAndTheyHaveOneTweet() throws Exception {
        UUID userId = UUID.randomUUID();
        Activity activity = createTweetActivity(0, userId, new DateTime());

        as.store(userId, activity);

        // Refresh to ensure the addition was committed.
        refreshIndex();

        List<Activity> activities = (List<Activity>) as.getByUser(userId, 1);
        assertEquals(activities.size(), 1);
        assertEquals(activities.get(0), activity);
    }

    @Test
    public void getLatestTwoTweetsByAUserGivenOneUserAndTheyHaveTwoTweets() throws Exception {
        UUID userId = UUID.randomUUID();
        int numTweets = 2;
        List<Activity> tweetActivities
                = (List<Activity>) createTweetActivities(userId, new DateTime(), numTweets);

        as.store(userId, tweetActivities);

        refreshIndex();

        List<Activity> activities = (List<Activity>) as.getByUser(userId, 2);
        assertEquals(activities.size(), numTweets);

        for (int i = 0; i < activities.size(); i++) {
            assertEquals(activities.get(i), tweetActivities.get(i));
        }
    }

    @Test
    public void getLatestTweetByAUserGivenOneUserAndTheyHaveTwoTweets() throws Exception {
        UUID userId = UUID.randomUUID();
        int numTweets = 2;
        List<Activity> tweetActivities
                = (List<Activity>) createTweetActivities(userId, new DateTime(), numTweets);

        as.store(userId, tweetActivities);

        refreshIndex();

        List<Activity> activities = (List<Activity>) as.getByUser(userId, 1);
        assertEquals(activities.size(), 1);
        assertEquals(activities.get(0), tweetActivities.get(0));
    }

    @Test
    public void getLatestTwoTweetsByAUserGivenOneUserAndTheyHaveOneTweet() throws Exception {
        UUID userId = UUID.randomUUID();
        Activity activity = createTweetActivity(0, userId, new DateTime());

        as.store(userId, activity);

        refreshIndex();

        List<Activity> activities = (List<Activity>) as.getByUser(userId, 2);
        assertEquals(activities.size(), 1);
        assertEquals(activities.get(0), activity);
    }

    @Test
    public void gettingLatestTweetsOfAUserShouldReturnTheMostRecentTweets() throws Exception {
        UUID userId = UUID.randomUUID();
        as.store(userId, createTweetActivities(userId, new DateTime(), 10));

        refreshIndex();

        Collection<Activity> activities = as.getByUser(userId, 3);
        assertEquals(activities.size(), 3);

        DateTime threeDaysAgo = new DateTime().minusDays(3);
        for (Activity activity : activities) {
            assertTrue(activity.getContext().getDate().isAfter(threeDaysAgo));
        }
    }

    @Test
    public void getLatestTweetOfAUserGivenUserHasOneTweetAndTwoTotalUsers() throws Exception {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        Activity activity = createTweetActivity(0, userId1, new DateTime());

        as.store(userId1, activity);
        as.store(userId2, createTweetActivity(1, userId2, new DateTime()));

        refreshIndex();

        List<Activity> activities = (List<Activity>) as.getByUser(userId1, 1);
        assertEquals(activities.size(), 1);
        assertEquals(activities.get(0), activity);
    }

    @Test
    public void getTweetsOfUserSpecifiedByDateRange() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        List<Activity> tweetActivities
                = (List<Activity>) createTweetActivities(userId, dateTime, 10);
        as.store(userId, tweetActivities);

        refreshIndex();

        Collection<Activity> userActivities = as.getByUser(userId, 10);
        List<Activity> activities
                = (List<Activity>) as.getByUserAndDateRange(userId, dateTime.minusDays(4), dateTime);
        assertEquals(userActivities.size(), 10);
        assertEquals(activities.size(), 5);

        for (int i = 0; i < activities.size(); i++) {
            assertEquals(activities.get(i), tweetActivities.get(i));
        }
    }

    @Test
    public void getTweetsOfAllUsersSpecifiedByDateRange() throws Exception {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        List<Activity> activities1
                = (List<Activity>) createTweetActivities(userId1, dateTime, 10);
        List<Activity> activities2
                = (List<Activity>) createTweetActivities(userId2, dateTime, 10);
        as.store(userId1, activities1);
        as.store(userId2, activities2);

        refreshIndex();

        Map<UUID, Collection<Activity>> allActivity
                = as.getByDateRange(dateTime.minusDays(5), dateTime);
        assertEquals(allActivity.size(), 2);

        List<Activity> activities = (List<Activity>) allActivity.get(userId1);
        assertEquals(activities.size(), 5);

        for (int i = 0; i < activities.size(); i++) {
            assertEquals(activities.get(i), activities1.get(i));
        }

        activities = (List<Activity>) allActivity.get(userId2);
        assertEquals(activities.size(), 5);

        for (int i = 0; i < activities.size(); i++) {
            assertEquals(activities.get(i), activities2.get(i));
        }
    }

    @Test
    public void getAllTweetsOfSpecifiedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        List<Activity> activitiesStored = (List<Activity>) createTweetActivities(userId, dateTime, 10);
        as.store(userId, activitiesStored);

        refreshIndex();

        List<Activity> activitiesRetrieved = (List<Activity>) as.getByUser(userId);
        assertEquals(activitiesRetrieved.size(), 10);

        for (int i = 0; i < activitiesRetrieved.size(); i++) {
            assertEquals(activitiesStored.get(i), activitiesRetrieved.get(i));
        }
    }

    @Test
    public void getTweetOfSpecifiedUserAndSpecifiedTweet() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();

        Activity activityStored = createTweetActivity(1, userId, new DateTime());
        activityStored.setId(activityId);
        as.store(userId, activityStored);

        refreshIndex();

        Activity activityRetrieved = as.getByUser(userId, activityId);
        assertEquals(activityRetrieved, activityStored);
    }

    @Test
    public void getTweetsOfSpecifiedUserAndSpecifiedTweets() throws Exception {
        UUID userId = UUID.randomUUID();
        Collection<UUID> activitiesIds = new ArrayList<UUID>();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        List<Activity> activitiesStored = (List<Activity>) createTweetActivities(userId, dateTime, 10);
        for(Activity a : activitiesStored) {
            UUID id = UUID.randomUUID();
            a.setId(id);
            activitiesIds.add(id);
        }
        as.store(userId, activitiesStored);

        refreshIndex();

        List<Activity> activitiesRetrieved = (List<Activity>) as.getByUser(userId, activitiesIds);
        assertEquals(activitiesRetrieved.size(), 10);

        for (int i = 0; i < activitiesRetrieved.size(); i++) {
            assertEquals(activitiesStored.get(i), activitiesRetrieved.get(i));
        }
    }

    @Test
    public void getFirstPageOfResultsOfTweetsForSpecifiedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        List<Activity> activitiesStored = (List<Activity>) createTweetActivities(userId, dateTime, 25);
        as.store(userId, activitiesStored);

        refreshIndex();

        List<Activity> activitiesRetrieved = (List<Activity>) as.getByUserPaginated(userId, 0, 10);
        assertEquals(activitiesRetrieved.size(), 10);

        for (int i = 0; i < activitiesRetrieved.size(); i++) {
            assertEquals(activitiesStored.get(i), activitiesRetrieved.get(i));
        }
    }

    @Test
    public void getSecondPageOfResultsOfTweetsForSpecifiedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        List<Activity> activitiesStored = (List<Activity>) createTweetActivities(userId, dateTime, 25);
        as.store(userId, activitiesStored);

        refreshIndex();

        List<Activity> activitiesRetrieved = (List<Activity>) as.getByUserPaginated(userId, 1, 10);
        assertEquals(activitiesRetrieved.size(), 10);

        int i = 10;
        for (Activity activity : activitiesRetrieved) {
            assertEquals(activitiesStored.get(i++), activity);
        }
    }

    @Test
    public void getPageOfResultsOfTweetsForSpecifiedUserWhenThereAreLessThanPageSizeActivities() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        List<Activity> activitiesStored = (List<Activity>) createTweetActivities(userId, dateTime, 25);
        as.store(userId, activitiesStored);

        refreshIndex();

        List<Activity> activitiesRetrieved = (List<Activity>) as.getByUserPaginated(userId, 2, 10);
        assertEquals(activitiesRetrieved.size(), 5);

        int i = 20;
        for (Activity activity : activitiesRetrieved) {
            assertEquals(activitiesStored.get(i++), activity);
        }
    }

    @Test
    public void searchForAllTweetsOfAUser() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        List<Activity> tweetsStored = (List<Activity>) createTweetActivities(userId, dateTime, 10);
        Collection<Activity> songsStored = createLastFmActivities(userId, dateTime, 10);
        as.store(userId, tweetsStored);
        as.store(userId, songsStored);

        refreshIndex();

        List<Activity> activitiesRetrieved =
                (List<Activity>) as.search("type", Verb.TWEET.name());

        assertEquals(activitiesRetrieved.size(), 10);
        for (int i = 0; i < activitiesRetrieved.size(); i++) {
            assertEquals(activitiesRetrieved.get(i), tweetsStored.get(i));
        }

        activitiesRetrieved =
                (List<Activity>) as.search("activity.object.type", Verb.TWEET.name());

        assertEquals(activitiesRetrieved.size(), 10);
        for (int i = 0; i < activitiesRetrieved.size(); i++) {
            assertEquals(activitiesRetrieved.get(i), tweetsStored.get(i));
        }
    }

    @Test
    public void searchForAllTweetsByTwitterUsername() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        List<Activity> tweetsStored = (List<Activity>) createTweetActivities(userId, dateTime, 10);
        Collection<Activity> songsStored = createLastFmActivities(userId, dateTime, 10);
        as.store(userId, tweetsStored);
        as.store(userId, songsStored);

        refreshIndex();

        List<Activity> activitiesRetrieved =
                (List<Activity>) as.search("username", "\"twitter-username\"");

        assertEquals(activitiesRetrieved.size(), 10);
        for (int i = 0; i < activitiesRetrieved.size(); i++) {
            assertEquals(activitiesRetrieved.get(i), tweetsStored.get(i));
        }

        activitiesRetrieved =
                (List<Activity>) as.search("activity.context.username", "\"twitter-username\"");

        assertEquals(activitiesRetrieved.size(), 10);
        for (int i = 0; i < activitiesRetrieved.size(); i++) {
            assertEquals(activitiesRetrieved.get(i), tweetsStored.get(i));
        }
    }

    @Test
    public void searchForAllTweetsYesterday() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);
        long dateTimeMillis = dateTime.minusDays(1).getMillis();

        List<Activity> tweetsStored =
                (List<Activity>) createTweetActivities(userId, dateTime, 10);
        as.store(userId, tweetsStored);

        refreshIndex();

        List<Activity> activitiesRetrieved =
                (List<Activity>) as.search("date", String.valueOf(dateTimeMillis));

        assertEquals(activitiesRetrieved.size(), 1);
        assertEquals(activitiesRetrieved.get(0), tweetsStored.get(1));

        activitiesRetrieved = (List<Activity>) as.search(
                "activity.context.date",
                String.valueOf(dateTimeMillis)
        );

        assertEquals(activitiesRetrieved.size(), 1);
        assertEquals(activitiesRetrieved.get(0), tweetsStored.get(1));
    }

    @Test
    public void searchWhenSpecifiedFieldIsSameAsAnotherField() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);
        Activity activity = createDuplicateFieldActivity();
        Activity tweetActivity = createTweetActivity(0, userId, dateTime);

        as.store(userId, activity);
        as.store(userId, tweetActivity);

        refreshIndex();

        List<Activity> activitiesRetrieved =
                (List<Activity>) as.search("activity.context.username", "\"twitter-username\"");

        assertEquals(activitiesRetrieved.size(), 1);
        assertEquals(activitiesRetrieved.get(0), tweetActivity);

        try {
            // This should throw an exception if the correct activity was retrieved
            // since no type mapping is specified for JSON serialization in the
            // Object class for the DuplicateFieldObject class.
            as.search("activity.object.username", "\"different-username\"");
        } catch (ActivityStoreException ignored) {
            return;
        }

        fail();
    }

    @Test(expectedExceptions = WildcardSearchException.class)
    public void wildcardSearchesShouldNotBeAllowed() throws Exception {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        as.store(userId1, createTweetActivities(userId1, dateTime, 5));
        as.store(userId2, createTweetActivities(userId2, dateTime, 5));

        refreshIndex();

        try {
            as.search("userId", "*");
        } catch (WildcardSearchException expected) {}

        try {
            as.search("*", "*");
        } catch (WildcardSearchException expected) {}

        try {
            as.search("user*", "*");
        } catch (WildcardSearchException expected) {}

        as.search("type", "tw*er");
    }

    private void refreshIndex() {
        // Refresh so we're looking at the latest version of the index.
        client.admin().indices().refresh(new RefreshRequest(INDEX_NAME)).actionGet();
    }

    private void clearIndex() throws Exception {
        client.prepareDeleteByQuery(INDEX_NAME)
                .setQuery(QueryBuilders.matchAllQuery())
                .execute().actionGet();

        // Wait for shards to settle (return to idle state).
        refreshIndex();
        client.admin().cluster().health(new ClusterHealthRequest(INDEX_NAME).waitForYellowStatus()).actionGet();
    }

    private Activity createTweetActivity(int tweetId, UUID userId,
                                         DateTime dateTime) throws Exception {
        ActivityBuilder ab = new DefaultActivityBuilder();

        ab.push();
        ab.setVerb(Verb.TWEET);
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("setText", "This is test tweet number " + tweetId
                + " from user " + userId + "!");
        ab.setObject(
                Tweet.class,
                new URL("http://twitter.com/#!/test-user/status/" + tweetId),
                "Test",
                fields
        );
        ab.setContext(
                dateTime.withZone(DateTimeZone.UTC).minusDays(tweetId),
                tweetServiceUrl,
                "twitter-username"
        );

        return ab.pop();
    }

    private Collection<Activity> createTweetActivities(
            UUID userId, DateTime dateTime, int numTweets
    ) throws Exception {
        Collection<Activity> activities = new ArrayList<Activity>(numTweets);

        for (int i = 0; i < numTweets; i++) {
            activities.add(createTweetActivity(i, userId, dateTime));
        }

        return activities;
    }

    private Activity createLastFmActivity(int trackId, UUID userId,
                                          DateTime dateTime) throws Exception {
        ActivityBuilder ab = new DefaultActivityBuilder();

        ab.push();
        ab.setVerb(Verb.LISTEN);
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("setMbid", UUID.randomUUID().toString());
        ab.setObject(
                Song.class,
                new URL("http://last.fm/" + userId + "/profile/listen/" + trackId),
                "My Song",
                fields);
        ab.setContext(
                dateTime.withZone(DateTimeZone.UTC).minusDays(trackId),
                lastFmServiceUrl,
                "lastfm-username"
        );

        return ab.pop();
    }

    private Collection<Activity> createLastFmActivities(
            UUID userId, DateTime dateTime, int numListens
    ) throws Exception {
        Collection<Activity> activities = new ArrayList<Activity>(numListens);

        for (int i = 0; i < numListens; i++) {
            activities.add(createLastFmActivity(i, userId, dateTime));
        }

        return activities;
    }

    private Activity createDuplicateFieldActivity() throws Exception {
        ActivityBuilder ab = new DefaultActivityBuilder();

        ab.push();
        ab.setVerb(Verb.TWEET);
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("setUsername", "different-username");
        ab.setObject(
                DuplicateFieldObject.class,
                new URL("http://duplica.te/test-user/"),
                "Test",
                fields
        );
        ab.setContext(
                new DateTime(DateTimeZone.UTC),
                "http://duplica.te",
                "dummy-username"
        );

        return ab.pop();
    }

    @SuppressWarnings("unchecked")
    private void assertHitEqualsTweetActivity(SearchHit hit, UUID userId, Activity activity) {
        assertEquals(hit.getType(), INDEX_TYPE);

        Map<String, Object> source = hit.getSource();
        assertEquals(source.get("userId"), userId.toString());

        Map<String, Object> esActivity = (Map<String, Object>) source.get("activity");
        assertEquals(esActivity.get("verb"), String.valueOf(Verb.TWEET));

        Map<String, Object> object = (Map<String, Object>) esActivity.get("object");
        Tweet tweet = (Tweet) activity.getObject();
        assertEquals(object.size(), 7);
        assertEquals(object.get("text"), tweet.getText());
        assertEquals(object.get("url"), tweet.getUrl().toString());
        assertEquals(object.get("hashTags"), new ArrayList<String>(tweet.getHashTags()));
        assertEquals(object.get("urls"), tweet.getUrls());
        assertEquals(object.get("name"), "Test");
        assertEquals(object.get("type"), Verb.TWEET.name());
        assertNull(object.get("description"));

        Map<String, Object> context = (Map<String, Object>) esActivity.get("context");
        Context tweetContext = activity.getContext();
        assertEquals(context.size(), 4);
        assertEquals(context.get("date"), tweetContext.getDate().getMillis());
        assertEquals(context.get("service"), tweetServiceUrl);
        assertEquals(context.get("username"), "twitter-username");
        assertNull(context.get("mood"));
    }

    private void assertHitsEqualTweetActivities(SearchHits hits, UUID userId, Collection<Activity> tweetActivities) {
        List<Activity> activities = new ArrayList<Activity>(tweetActivities);
        int numTweets = activities.size();
        assertEquals(hits.getTotalHits(), numTweets);

        for (int i = 0; i < numTweets; i++) {
            SearchHit hit = hits.getAt(i);
            assertHitEqualsTweetActivity(hit, userId, activities.get(i));
        }
    }

    @SuppressWarnings("unchecked")
    private void assertHitEqualsLastFmActivity(SearchHit hit, UUID userId, Activity activity) {
        assertEquals(hit.getType(), INDEX_TYPE);

        Map<String, Object> source = hit.getSource();
        assertEquals(source.get("userId"), userId.toString());

        Map<String, Object> esActivity = (Map<String, Object>) source.get("activity");
        assertEquals(esActivity.get("verb"), String.valueOf(Verb.LISTEN));

        Map<String, Object> object = (Map<String, Object>) esActivity.get("object");
        Song song = (Song) activity.getObject();
        assertEquals(object.size(), 5);
        assertEquals(object.get("mbid"), song.getMbid());
        assertEquals(object.get("url"), song.getUrl().toString());
        assertEquals(object.get("name"), "My Song");
        assertEquals(object.get("type"), Verb.SONG.name());
        assertNull(object.get("description"));

        Map<String, Object> context = (Map<String, Object>) esActivity.get("context");
        Context songContext = activity.getContext();
        assertEquals(context.size(), 4);
        assertEquals(context.get("date"), songContext.getDate().getMillis());
        assertEquals(context.get("service"), lastFmServiceUrl);
        assertEquals(context.get("username"), "lastfm-username");
        assertNull(context.get("mood"));
    }

    private void assertHitsEqualLastFmActivities(SearchHits hits, UUID userId,
                                                 Collection<Activity> lastFmActivities) {
        List<Activity> activities = new ArrayList<Activity>(lastFmActivities);
        int numListens = activities.size();
        assertEquals(hits.getTotalHits(), numListens);

        for (int i = 0; i < numListens; i++) {
            SearchHit hit = hits.getAt(i);
            assertHitEqualsLastFmActivity(hit, userId, activities.get(i));
        }
    }
}
