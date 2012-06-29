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
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.ActivityBuilder;
import tv.notube.commons.model.activity.Context;
import tv.notube.commons.model.activity.DefaultActivityBuilder;
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
import static tv.notube.activities.ElasticSearchActivityStoreImpl.INDEX_NAME;
import static tv.notube.activities.ElasticSearchActivityStoreImpl.INDEX_TYPE;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 * @author Alex Cowell ( alxcwll@gmail.com )
 */
public class ElasticSearchActivityStoreIntegrationTest {

    private ActivityStore as;

    private Node node;
    private Client client;
    private String tweetServiceUrl = "http://twitter.com";

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

        // TODO: Remove the ES data dir
    }

    @BeforeTest
    public void setUp() throws Exception {
        as = ElasticSearchActivityStoreFactory.getInstance().build();
    }

    @AfterTest
    public void tearDown() throws Exception {
        ((ElasticSearchActivityStoreImpl) as).closeClient();
        as = null;
    }

    @Test
    public void storeASingleTweetForAUser() throws Exception {
        clearIndex();

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
    public void storeMultipleTweetsForAUser() throws Exception {
        clearIndex();

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
    public void getLatestTweetByAUserGivenOneUserAndTheyHaveOneTweet() throws Exception {
        clearIndex();

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
        clearIndex();

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
        clearIndex();

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
        clearIndex();

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
        clearIndex();

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
        clearIndex();

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
        clearIndex();

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
        clearIndex();

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
        clearIndex();

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
        clearIndex();

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
        clearIndex();

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
                new URL(tweetServiceUrl)
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
        assertEquals(object.get("@class"), Tweet.class.getName());
        assertNull(object.get("description"));

        Map<String, Object> context = (Map<String, Object>) esActivity.get("context");
        Context tweetContext = activity.getContext();
        assertEquals(context.size(), 3);
        assertEquals(context.get("date"), tweetContext.getDate().getMillis());
        assertEquals(context.get("service"), tweetServiceUrl);
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
}
