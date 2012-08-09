package tv.notube.activities;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
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
import tv.notube.commons.model.User;
import tv.notube.commons.model.activity.*;
import tv.notube.commons.model.auth.OAuthAuth;

import java.io.File;
import java.io.IOException;
import java.lang.Object;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static tv.notube.activities.ElasticSearchActivityStore.INDEX_NAME;
import static tv.notube.activities.ElasticSearchActivityStore.INDEX_TYPE;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 * @author Alex Cowell ( alxcwll@gmail.com )
 */
public class ElasticSearchActivityStoreTest {

    private ActivityStore as;

    private Node node;
    private Client client;
    private static String ascOrder = SortOrder.ASC.toString();
    private static String descOrder = SortOrder.DESC.toString();
    private final String tweetServiceUrl = "http://twitter.com";
    private final String lastFmServiceUrl = "http://last.fm";
    private final String esDirectory = "es";

    @BeforeSuite
    public void beforeSuite() throws Exception {
        node = NodeBuilder.nodeBuilder()
                .settings(ImmutableSettings.settingsBuilder()
                        .put("path.home", esDirectory)
                )
                .node();
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
        delete(new File(esDirectory));
        delete(new File("logs"));
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
        ResolvedActivity tweetActivity = createTweetActivity(0, userId, dateTime);

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
        ResolvedActivity lastFmActivity = createLastFmActivity(0, userId, dateTime);

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

        Collection<ResolvedActivity> tweetActivities = createTweetActivities(userId, dateTime, numTweets);
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

        Collection<ResolvedActivity> lastFmActivities
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
        ResolvedActivity activity = createTweetActivity(0, userId, new DateTime());

        as.store(userId, activity);

        // Refresh to ensure the addition was committed.
        refreshIndex();

        List<ResolvedActivity> activities = (List<ResolvedActivity>) as.getByUser(userId, 1);
        assertEquals(activities.size(), 1);
        assertEquals(activities.get(0), activity);
    }

    @Test
    public void getLatestTwoTweetsByAUserGivenOneUserAndTheyHaveTwoTweets() throws Exception {
        UUID userId = UUID.randomUUID();
        int numTweets = 2;
        List<ResolvedActivity> tweetActivities
                = (List<ResolvedActivity>) createTweetActivities(userId, new DateTime(), numTweets);

        as.store(userId, tweetActivities);

        refreshIndex();

        List<ResolvedActivity> activities = (List<ResolvedActivity>) as.getByUser(userId, 2);
        assertEquals(activities.size(), numTweets);

        for (int i = 0; i < activities.size(); i++) {
            assertEquals(activities.get(i), tweetActivities.get(i));
        }
    }

    @Test
    public void getLatestTweetByAUserGivenOneUserAndTheyHaveTwoTweets() throws Exception {
        UUID userId = UUID.randomUUID();
        int numTweets = 2;
        List<ResolvedActivity> tweetActivities
                = (List<ResolvedActivity>) createTweetActivities(userId, new DateTime(), numTweets);

        as.store(userId, tweetActivities);

        refreshIndex();

        List<ResolvedActivity> activities = (List<ResolvedActivity>) as.getByUser(userId, 1);
        assertEquals(activities.size(), 1);
        assertEquals(activities.get(0), tweetActivities.get(0));
    }

    @Test
    public void getLatestTwoTweetsByAUserGivenOneUserAndTheyHaveOneTweet() throws Exception {
        UUID userId = UUID.randomUUID();
        ResolvedActivity activity = createTweetActivity(0, userId, new DateTime());

        as.store(userId, activity);

        refreshIndex();

        List<ResolvedActivity> activities = (List<ResolvedActivity>) as.getByUser(userId, 2);
        assertEquals(activities.size(), 1);
        assertEquals(activities.get(0), activity);
    }

    @Test
    public void gettingLatestTweetsOfAUserShouldReturnTheMostRecentTweets() throws Exception {
        UUID userId = UUID.randomUUID();
        as.store(userId, createTweetActivities(userId, new DateTime(), 10));

        refreshIndex();

        Collection<ResolvedActivity> activities = as.getByUser(userId, 3);
        assertEquals(activities.size(), 3);

        DateTime threeDaysAgo = new DateTime().minusDays(3);
        for (ResolvedActivity activity : activities) {
            assertTrue(activity.getActivity().getContext().getDate().isAfter(threeDaysAgo));
        }
    }

    @Test
    public void getLatestTweetOfAUserGivenUserHasOneTweetAndTwoTotalUsers() throws Exception {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        ResolvedActivity activity = createTweetActivity(0, userId1, new DateTime());

        as.store(userId1, activity);
        as.store(userId2, createTweetActivity(1, userId2, new DateTime()));

        refreshIndex();

        List<ResolvedActivity> activities = (List<ResolvedActivity>) as.getByUser(userId1, 1);
        assertEquals(activities.size(), 1);
        assertEquals(activities.get(0), activity);
    }

    @Test
    public void getTweetsOfUserSpecifiedByDateRange() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        List<ResolvedActivity> tweetActivities
                = (List<ResolvedActivity>) createTweetActivities(userId, dateTime, 10);
        as.store(userId, tweetActivities);

        refreshIndex();

        Collection<ResolvedActivity> userActivities = as.getByUser(userId, 10);
        List<ResolvedActivity> activities
                = (List<ResolvedActivity>) as.getByUserAndDateRange(userId, dateTime.minusDays(4), dateTime);
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

        List<ResolvedActivity> activities1
                = (List<ResolvedActivity>) createTweetActivities(userId1, dateTime, 10);
        List<ResolvedActivity> activities2
                = (List<ResolvedActivity>) createTweetActivities(userId2, dateTime, 10);
        as.store(userId1, activities1);
        as.store(userId2, activities2);

        refreshIndex();

        Map<UUID, Collection<ResolvedActivity>> allActivity
                = as.getByDateRange(dateTime.minusDays(5), dateTime);
        assertEquals(allActivity.size(), 2);

        List<ResolvedActivity> activities = (List<ResolvedActivity>) allActivity.get(userId1);
        assertEquals(activities.size(), 5);

        for (int i = 0; i < activities.size(); i++) {
            assertEquals(activities.get(i), activities1.get(i));
        }

        activities = (List<ResolvedActivity>) allActivity.get(userId2);
        assertEquals(activities.size(), 5);

        for (int i = 0; i < activities.size(); i++) {
            assertEquals(activities.get(i), activities2.get(i));
        }
    }

    @Test
    public void getSpecificTweetOfSpecifiedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();

        ResolvedActivity activityStored = createTweetActivity(1, userId, new DateTime());
        activityStored.getActivity().setId(activityId);
        as.store(userId, activityStored);

        refreshIndex();

        ResolvedActivity activityRetrieved = as.getActivity(activityId);
        assertEquals(activityRetrieved, activityStored);
    }

    @Test
    public void getTweetsOfSpecifiedUserAndSpecifiedTweets() throws Exception {
        UUID userId = UUID.randomUUID();
        Collection<UUID> activitiesIds = new ArrayList<UUID>();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        List<ResolvedActivity> activitiesStored = (List<ResolvedActivity>) createTweetActivities(userId, dateTime, 10);
        for (int i = 0; i < activitiesStored.size() - 5; i++) {
            UUID id = UUID.randomUUID();
            activitiesStored.get(i).getActivity().setId(id);
            activitiesIds.add(id);
        }
        as.store(userId, activitiesStored);

        refreshIndex();

        List<ResolvedActivity> activitiesRetrieved = (List<ResolvedActivity>) as.getByUser(userId, activitiesIds);
        assertEquals(activitiesRetrieved.size(), 5);

        for (int i = 0; i < activitiesRetrieved.size(); i++) {
            assertEquals(activitiesStored.get(i), activitiesRetrieved.get(i));
        }
    }

    @Test(enabled = false)
    public void getTweetsOfSpecifiedUserAndLargeNumberOfSpecifiedTweets() throws Exception {
        UUID userId = UUID.randomUUID();
        Collection<UUID> activitiesIds = new ArrayList<UUID>();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        List<ResolvedActivity> activitiesStored = (List<ResolvedActivity>) createTweetActivities(userId, dateTime, 100);
        for (int i = 0; i < activitiesStored.size() - 50; i++) {
            UUID id = UUID.randomUUID();
            activitiesStored.get(i).getActivity().setId(id);
            activitiesIds.add(id);
        }
        as.store(userId, activitiesStored);

        refreshIndex();

        List<ResolvedActivity> activitiesRetrieved = (List<ResolvedActivity>) as.getByUser(userId, activitiesIds);
        assertEquals(activitiesRetrieved.size(), 50);

        for (int i = 0; i < activitiesRetrieved.size(); i++) {
            assertEquals(activitiesStored.get(i), activitiesRetrieved.get(i));
        }
    }

    @Test
    public void getFirstPageOfResultsOfTweetsForSpecifiedUserDescending() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        List<ResolvedActivity> activitiesStored = (List<ResolvedActivity>) createTweetActivities(userId, dateTime, 25);
        as.store(userId, activitiesStored);

        refreshIndex();

        List<ResolvedActivity> activitiesRetrieved =
                (List<ResolvedActivity>) as.getByUserPaginated(userId, 0, 10, descOrder);
        assertEquals(activitiesRetrieved.size(), 10);

        for (int i = 0; i < activitiesRetrieved.size(); i++) {
            assertEquals(activitiesRetrieved.get(i), activitiesStored.get(i));
        }
    }

    @Test
    public void getSecondPageOfResultsOfTweetsForSpecifiedUserDescending() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        List<ResolvedActivity> activitiesStored = (List<ResolvedActivity>) createTweetActivities(userId, dateTime, 25);
        as.store(userId, activitiesStored);

        refreshIndex();

        List<ResolvedActivity> activitiesRetrieved =
                (List<ResolvedActivity>) as.getByUserPaginated(userId, 1, 10, descOrder);
        assertEquals(activitiesRetrieved.size(), 10);

        int i = 10;
        for (ResolvedActivity activity : activitiesRetrieved) {
            assertEquals(activity, activitiesStored.get(i++));
        }
    }

    @Test
    public void getPageOfResultsOfTweetsForSpecifiedUserWhenThereAreLessThanPageSizeActivitiesDescending() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        List<ResolvedActivity> activitiesStored = (List<ResolvedActivity>) createTweetActivities(userId, dateTime, 25);
        as.store(userId, activitiesStored);

        refreshIndex();

        List<ResolvedActivity> activitiesRetrieved =
                (List<ResolvedActivity>) as.getByUserPaginated(userId, 2, 10, descOrder);
        assertEquals(activitiesRetrieved.size(), 5);

        int i = 20;
        for (ResolvedActivity activity : activitiesRetrieved) {
            assertEquals(activity, activitiesStored.get(i++));
        }
    }

    @Test
    public void getFirstPageOfResultsOfTweetsForSpecifiedUserAscending() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        List<ResolvedActivity> activitiesStored = (List<ResolvedActivity>) createTweetActivities(userId, dateTime, 25);
        as.store(userId, activitiesStored);

        refreshIndex();

        List<ResolvedActivity> activitiesRetrieved =
                (List<ResolvedActivity>) as.getByUserPaginated(userId, 0, 10, ascOrder);
        assertEquals(activitiesRetrieved.size(), 10);

        for (int i = 0; i < activitiesRetrieved.size(); i++) {
            assertEquals(
                    activitiesRetrieved.get(i),
                    activitiesStored.get(activitiesStored.size() - 1 - i)
            );
        }
    }

    @Test
    public void getSecondPageOfResultsOfTweetsForSpecifiedUserAscending() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        List<ResolvedActivity> activitiesStored =
                (List<ResolvedActivity>) createTweetActivities(userId, dateTime, 25);
        as.store(userId, activitiesStored);

        refreshIndex();

        List<ResolvedActivity> activitiesRetrieved =
                (List<ResolvedActivity>) as.getByUserPaginated(userId, 1, 10, ascOrder);
        assertEquals(activitiesRetrieved.size(), 10);

        int i = 10;
        for (ResolvedActivity activity : activitiesRetrieved) {
            assertEquals(
                    activity,
                    activitiesStored.get(activitiesStored.size() - 1 - i++)
            );
        }
    }

    @Test
    public void getPageOfResultsOfTweetsForSpecifiedUserWhenThereAreLessThanPageSizeActivitiesAscending() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        List<ResolvedActivity> activitiesStored = (List<ResolvedActivity>) createTweetActivities(userId, dateTime, 25);
        as.store(userId, activitiesStored);

        refreshIndex();

        List<ResolvedActivity> activitiesRetrieved =
                (List<ResolvedActivity>) as.getByUserPaginated(userId, 2, 10, ascOrder);
        assertEquals(activitiesRetrieved.size(), 5);

        int i = 20;
        for (ResolvedActivity activity : activitiesRetrieved) {
            assertEquals(
                    activity,
                    activitiesStored.get(activitiesStored.size() - 1 - i++)
            );
        }
    }

    @Test(expectedExceptions = InvalidOrderException.class)
    public void getByUserWithInvalidSortOrderShouldThrowException() throws Exception {
        as.getByUserPaginated(UUID.randomUUID(), 0, 10, "not-an-order");
    }

    @Test
    public void searchForAllTweetsOfAUserDescending() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        List<ResolvedActivity> tweetsStored = (List<ResolvedActivity>) createTweetActivities(userId, dateTime, 10);
        Collection<ResolvedActivity> songsStored = createLastFmActivities(userId, dateTime, 10);
        as.store(userId, tweetsStored);
        as.store(userId, songsStored);

        refreshIndex();

        List<ResolvedActivity> activitiesRetrieved = (List<ResolvedActivity>) as.search(
                "type",
                Verb.TWEET.name(),
                0,
                10,
                descOrder
        );

        assertEquals(activitiesRetrieved.size(), 10);
        for (int i = 0; i < activitiesRetrieved.size(); i++) {
            assertEquals(activitiesRetrieved.get(i), tweetsStored.get(i));
        }

        activitiesRetrieved = (List<ResolvedActivity>) as.search(
                "activity.object.type",
                Verb.TWEET.name(),
                0,
                10,
                descOrder
        );

        assertEquals(activitiesRetrieved.size(), 10);
        for (int i = 0; i < activitiesRetrieved.size(); i++) {
            assertEquals(activitiesRetrieved.get(i), tweetsStored.get(i));
        }
    }

    @Test
    public void searchForAllTweetsByTwitterUsernameDescending() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        List<ResolvedActivity> tweetsStored = (List<ResolvedActivity>) createTweetActivities(userId, dateTime, 10);
        Collection<ResolvedActivity> songsStored = createLastFmActivities(userId, dateTime, 10);
        as.store(userId, tweetsStored);
        as.store(userId, songsStored);

        refreshIndex();

        List<ResolvedActivity> activitiesRetrieved = (List<ResolvedActivity>) as.search(
                "username",
                "\"twitter-username\"",
                0,
                20,
                descOrder
        );

        assertEquals(activitiesRetrieved.size(), 10);
        for (int i = 0; i < activitiesRetrieved.size(); i++) {
            assertEquals(activitiesRetrieved.get(i), tweetsStored.get(i));
        }

        activitiesRetrieved = (List<ResolvedActivity>) as.search(
                "activity.context.username",
                "\"twitter-username\"",
                0,
                20,
                descOrder);

        assertEquals(activitiesRetrieved.size(), 10);
        for (int i = 0; i < activitiesRetrieved.size(); i++) {
            assertEquals(activitiesRetrieved.get(i), tweetsStored.get(i));
        }
    }

    @Test
    public void searchForAllTweetsYesterdayDescending() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);
        long dateTimeMillis = dateTime.minusDays(1).getMillis();

        List<ResolvedActivity> tweetsStored =
                (List<ResolvedActivity>) createTweetActivities(userId, dateTime, 10);
        as.store(userId, tweetsStored);

        refreshIndex();

        List<ResolvedActivity> activitiesRetrieved = (List<ResolvedActivity>) as.search(
                "date",
                String.valueOf(dateTimeMillis),
                0,
                20,
                descOrder
        );

        assertEquals(activitiesRetrieved.size(), 1);
        assertEquals(activitiesRetrieved.get(0), tweetsStored.get(1));

        activitiesRetrieved = (List<ResolvedActivity>) as.search(
                "activity.context.date",
                String.valueOf(dateTimeMillis),
                0,
                20,
                descOrder
        );

        assertEquals(activitiesRetrieved.size(), 1);
        assertEquals(activitiesRetrieved.get(0), tweetsStored.get(1));
    }

    @Test
    public void searchWhenSpecifiedFieldIsSameAsAnotherFieldDescending() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);
        ResolvedActivity activity = createDuplicateFieldActivity();
        ResolvedActivity tweetActivity = createTweetActivity(0, userId, dateTime);

        as.store(userId, activity);
        as.store(userId, tweetActivity);

        refreshIndex();

        List<ResolvedActivity> activitiesRetrieved = (List<ResolvedActivity>) as.search(
                "activity.context.username",
                "\"twitter-username\"",
                0,
                10,
                descOrder
        );

        assertEquals(activitiesRetrieved.size(), 1);
        assertEquals(activitiesRetrieved.get(0), tweetActivity);

        try {
            // This should throw an exception if the correct activity was retrieved
            // since no type mapping is specified for JSON serialization in the
            // Object class for the DuplicateFieldObject class.
            as.search(
                    "activity.object.username",
                    "\"different-username\"",
                    0,
                    10,
                    descOrder);
        } catch (ActivityStoreException ignored) {
            return;
        }

        fail();
    }

    @Test
    public void searchForAllTweetsOfAUserAscending() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        List<ResolvedActivity> tweetsStored = (List<ResolvedActivity>) createTweetActivities(userId, dateTime, 10);
        Collection<ResolvedActivity> songsStored = createLastFmActivities(userId, dateTime, 10);
        as.store(userId, tweetsStored);
        as.store(userId, songsStored);

        refreshIndex();

        List<ResolvedActivity> activitiesRetrieved = (List<ResolvedActivity>) as.search(
                "type",
                Verb.TWEET.name(),
                0,
                10,
                ascOrder
        );

        assertEquals(activitiesRetrieved.size(), 10);
        for (int i = 0; i < activitiesRetrieved.size(); i++) {
            assertEquals(
                    activitiesRetrieved.get(i),
                    tweetsStored.get(tweetsStored.size() - 1 - i)
            );
        }

        activitiesRetrieved = (List<ResolvedActivity>) as.search(
                "activity.object.type",
                Verb.TWEET.name(),
                0,
                10,
                ascOrder
        );

        assertEquals(activitiesRetrieved.size(), 10);
        for (int i = 0; i < activitiesRetrieved.size(); i++) {
            assertEquals(
                    activitiesRetrieved.get(i),
                    tweetsStored.get(tweetsStored.size() - 1 - i)
            );
        }
    }

    @Test
    public void searchForAllTweetsByTwitterUsernameAscending() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        List<ResolvedActivity> tweetsStored = (List<ResolvedActivity>) createTweetActivities(userId, dateTime, 10);
        Collection<ResolvedActivity> songsStored = createLastFmActivities(userId, dateTime, 10);
        as.store(userId, tweetsStored);
        as.store(userId, songsStored);

        refreshIndex();

        List<ResolvedActivity> activitiesRetrieved = (List<ResolvedActivity>) as.search(
                "username",
                "\"twitter-username\"",
                0,
                20,
                ascOrder
        );

        assertEquals(activitiesRetrieved.size(), 10);
        for (int i = 0; i < activitiesRetrieved.size(); i++) {
            assertEquals(
                    activitiesRetrieved.get(i),
                    tweetsStored.get(tweetsStored.size() - 1 - i)
            );
        }

        activitiesRetrieved = (List<ResolvedActivity>) as.search(
                "activity.context.username",
                "\"twitter-username\"",
                0,
                20,
                ascOrder);

        assertEquals(activitiesRetrieved.size(), 10);
        for (int i = 0; i < activitiesRetrieved.size(); i++) {
            assertEquals(
                    activitiesRetrieved.get(i),
                    tweetsStored.get(tweetsStored.size() - 1 - i)
            );
        }
    }

    @Test
    public void searchForAllTweetsYesterdayAscending() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);
        long dateTimeMillis = dateTime.minusDays(1).getMillis();

        List<ResolvedActivity> tweetsStored =
                (List<ResolvedActivity>) createTweetActivities(userId, dateTime, 10);
        as.store(userId, tweetsStored);

        refreshIndex();

        List<ResolvedActivity> activitiesRetrieved = (List<ResolvedActivity>) as.search(
                "date",
                String.valueOf(dateTimeMillis),
                0,
                20,
                ascOrder
        );

        assertEquals(activitiesRetrieved.size(), 1);
        assertEquals(activitiesRetrieved.get(0), tweetsStored.get(1));

        activitiesRetrieved = (List<ResolvedActivity>) as.search(
                "activity.context.date",
                String.valueOf(dateTimeMillis),
                0,
                20,
                ascOrder
        );

        assertEquals(activitiesRetrieved.size(), 1);
        assertEquals(activitiesRetrieved.get(0), tweetsStored.get(1));
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
            as.search("userId", "*", 0, 10, descOrder);
        } catch (WildcardSearchException expected) {}

        try {
            as.search("*", "*", 0, 10, descOrder);
        } catch (WildcardSearchException expected) {}

        try {
            as.search("user*", "*", 0, 10, ascOrder);
        } catch (WildcardSearchException expected) {}

        as.search("type", "tw*er", 0, 10, ascOrder);
    }

    @Test(expectedExceptions = InvalidOrderException.class)
    public void searchingWithInvalidSortOrderShouldThrowException() throws Exception {
        as.search("type", "TWEET", 0, 10, "not-an-order");
    }

    @Test
    public void hiddenActivityShouldNotBeIncludedInSearchResults() throws Exception {
        ResolvedActivity hiddenActivity = createTweetActivity(0, UUID.randomUUID(), new DateTime());
        hiddenActivity.setVisible(false);

        as.store(UUID.randomUUID(), hiddenActivity);
        refreshIndex();

        List<ResolvedActivity> activitiesRetrieved = (List<ResolvedActivity>) as.search(
                "type",
                Verb.TWEET.name(),
                0,
                10,
                descOrder
        );

        assertTrue(activitiesRetrieved.isEmpty());
    }

    @Test
    public void hiddenActivityShouldNotBeIncludedInPaginatedUserActivities() throws Exception {
        UUID userId = UUID.randomUUID();
        ResolvedActivity hiddenActivity = createTweetActivity(0, userId, new DateTime());
        hiddenActivity.setVisible(false);

        as.store(userId, hiddenActivity);
        refreshIndex();

        List<ResolvedActivity> activitiesRetrieved =
                (List<ResolvedActivity>) as.getByUserPaginated(userId, 0, 10, descOrder);

        assertTrue(activitiesRetrieved.isEmpty());
    }

    @Test
    public void hiddenActivityShouldNotBeIncludedInAUsersLatestActivities() throws Exception {
        UUID userId = UUID.randomUUID();
        ResolvedActivity hiddenActivity = createTweetActivity(0, userId, new DateTime());
        hiddenActivity.setVisible(false);

        as.store(userId, hiddenActivity);
        refreshIndex();

        List<ResolvedActivity> activitiesRetrieved =
                (List<ResolvedActivity>) as.getByUser(userId, 10);

        assertTrue(activitiesRetrieved.isEmpty());
    }

    @Test
    public void hiddenActivityShouldNotBeIncludedInDateRangeOfUserActivities() throws Exception {
        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        ResolvedActivity hiddenActivity = createTweetActivity(0, userId, dateTime.minusDays(2));
        hiddenActivity.setVisible(false);

        as.store(userId, hiddenActivity);
        refreshIndex();

        List<ResolvedActivity> activitiesRetrieved =
                (List<ResolvedActivity>) as.getByUserAndDateRange(userId, dateTime.minusDays(4), dateTime);

        assertTrue(activitiesRetrieved.isEmpty());
    }

    @Test
    public void hiddenActivityShouldNotBeIncludedInResultsOfDateRangeQuery() throws Exception {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        ResolvedActivity hiddenActivity = createTweetActivity(0, userId1, dateTime.minusDays(2));
        hiddenActivity.setVisible(false);

        as.store(userId1, createTweetActivities(userId1, dateTime, 10));
        as.store(userId1, hiddenActivity);
        as.store(userId2, createTweetActivities(userId2, dateTime, 10));

        refreshIndex();

        Map<UUID, Collection<ResolvedActivity>> allActivity
                = as.getByDateRange(dateTime.minusDays(5), dateTime);
        assertEquals(allActivity.size(), 2);

        List<ResolvedActivity> activities = (List<ResolvedActivity>) allActivity.get(userId1);
        assertEquals(activities.size(), 5);

        activities = (List<ResolvedActivity>) allActivity.get(userId2);
        assertEquals(activities.size(), 5);
    }

    @Test
    public void gettingASingleHiddenActivityShouldReturnNull() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();

        ResolvedActivity hiddenActivity = createTweetActivity(0, userId, DateTime.now());
        hiddenActivity.setVisible(false);
        hiddenActivity.getActivity().setId(activityId);

        as.store(userId, hiddenActivity);
        refreshIndex();

        assertNull(as.getActivity(activityId));
    }

    @Test
    public void hiddenActivityShouldNotBeIncludedInResultsOfActivityIdsQuery() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        Collection<UUID> activitiesIds = new ArrayList<UUID>();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        ResolvedActivity hiddenActivity = createTweetActivity(13, userId, DateTime.now());
        hiddenActivity.setVisible(false);
        hiddenActivity.getActivity().setId(activityId);
        activitiesIds.add(activityId);

        List<ResolvedActivity> activitiesStored = (List<ResolvedActivity>) createTweetActivities(userId, dateTime, 10);
        for (int i = 0; i < activitiesStored.size() - 5; i++) {
            UUID id = UUID.randomUUID();
            activitiesStored.get(i).getActivity().setId(id);
            activitiesIds.add(id);
        }
        as.store(userId, activitiesStored);
        as.store(userId, hiddenActivity);

        refreshIndex();

        List<ResolvedActivity> activitiesRetrieved = (List<ResolvedActivity>) as.getByUser(userId, activitiesIds);
        assertEquals(activitiesRetrieved.size(), 5);
    }

    @Test
    public void aVisibleActivityCanBeHidden() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        ResolvedActivity activity = createTweetActivity(0, userId, dateTime);
        activity.getActivity().setId(activityId);

        as.store(userId, activity);
        refreshIndex();

        assertNotNull(as.getActivity(activityId));

        as.setVisible(activityId, false);
        refreshIndex();

        assertNull(as.getActivity(activityId));
    }

    @Test
    public void aHiddenActivityCanBeMadeVisible() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        DateTime dateTime = new DateTime(DateTimeZone.UTC);

        ResolvedActivity hiddenActivity = createTweetActivity(0, userId, dateTime);
        hiddenActivity.getActivity().setId(activityId);
        hiddenActivity.setVisible(false);

        as.store(userId, hiddenActivity);
        refreshIndex();

        assertNull(as.getActivity(activityId));

        as.setVisible(activityId, true);
        refreshIndex();

        assertNotNull(as.getActivity(activityId));
    }

    @Test(expectedExceptions = ActivityStoreException.class)
    public void attemptingToSetTheVisibilityOfANonExistentActivityThrowsException() throws Exception {
        as.setVisible(UUID.randomUUID(), false);
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

    private User getUser() {
        User user = new User();
        user.setName("test-name");
        user.setSurname("test-surname");
        user.setPassword("test-pwd");
        user.setUsername("test-username");
        user.addService("test-service", new OAuthAuth("s", "c"));
        return user;
    }

    private ResolvedActivity createTweetActivity(
            int tweetId,
            UUID userId,
            DateTime dateTime
    ) throws Exception {
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
        Activity activity = ab.pop();
        return new ResolvedActivity(userId, activity, getUser());
    }

    private Collection<ResolvedActivity> createTweetActivities(
            UUID userId, DateTime dateTime, int numTweets
    ) throws Exception {
        Collection<ResolvedActivity> activities = new ArrayList<ResolvedActivity>(numTweets);

        for (int i = 0; i < numTweets; i++) {
            activities.add(createTweetActivity(i, userId, dateTime));
        }

        return activities;
    }

    private ResolvedActivity createLastFmActivity(
            int trackId,
            UUID userId,
            DateTime dateTime
    ) throws Exception {
        ActivityBuilder ab = new DefaultActivityBuilder();

        ab.push();
        ab.setVerb(Verb.LISTEN);
        Map<String, java.lang.Object> fields = new HashMap<String, Object>();
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
        Activity activity = ab.pop();
        return new ResolvedActivity(UUID.randomUUID(), activity, getUser());
    }

    private Collection<ResolvedActivity> createLastFmActivities(
            UUID userId, DateTime dateTime, int numListens
    ) throws Exception {
        Collection<ResolvedActivity> activities = new ArrayList<ResolvedActivity>(numListens);

        for (int i = 0; i < numListens; i++) {
            activities.add(createLastFmActivity(i, userId, dateTime));
        }

        return activities;
    }

    private ResolvedActivity createDuplicateFieldActivity() throws Exception {
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
        Activity activity = ab.pop();
        return new ResolvedActivity(UUID.randomUUID(), activity, getUser());
    }

    @SuppressWarnings("unchecked")
    private void assertHitEqualsTweetActivity(SearchHit hit, UUID userId, ResolvedActivity activity) {
        assertEquals(hit.getType(), INDEX_TYPE);

        Map<String, Object> source = hit.getSource();
        assertEquals(source.get("userId"), userId.toString());

        Map<String, Object> esActivity = (Map<String, Object>) source.get("activity");
        assertEquals(esActivity.get("verb"), String.valueOf(Verb.TWEET));

        Map<String, Object> object = (Map<String, Object>) esActivity.get("object");
        Tweet tweet = (Tweet) activity.getActivity().getObject();
        assertEquals(object.size(), 7);
        assertEquals(object.get("text"), tweet.getText());
        assertEquals(object.get("url"), tweet.getUrl().toString());
        assertEquals(object.get("hashTags"), new ArrayList<String>(tweet.getHashTags()));
        assertEquals(object.get("urls"), tweet.getUrls());
        assertEquals(object.get("name"), "Test");
        assertEquals(object.get("type"), Verb.TWEET.name());
        assertNull(object.get("description"));

        Map<String, Object> context = (Map<String, Object>) esActivity.get("context");
        Context tweetContext = activity.getActivity().getContext();
        assertEquals(context.size(), 4);
        assertEquals(context.get("date"), tweetContext.getDate().getMillis());
        assertEquals(context.get("service"), tweetServiceUrl);
        assertEquals(context.get("username"), "twitter-username");
        assertNull(context.get("mood"));
    }

    private void assertHitsEqualTweetActivities(SearchHits hits, UUID userId, Collection<ResolvedActivity> tweetActivities) {
        List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>(tweetActivities);
        int numTweets = activities.size();
        assertEquals(hits.getTotalHits(), numTweets);

        for (int i = 0; i < numTweets; i++) {
            SearchHit hit = hits.getAt(i);
            assertHitEqualsTweetActivity(hit, userId, activities.get(i));
        }
    }

    @SuppressWarnings("unchecked")
    private void assertHitEqualsLastFmActivity(SearchHit hit, UUID userId, ResolvedActivity activity) {
        assertEquals(hit.getType(), INDEX_TYPE);

        Map<String, Object> source = hit.getSource();
        assertEquals(source.get("userId"), userId.toString());

        Map<String, Object> esActivity = (Map<String, Object>) source.get("activity");
        assertEquals(esActivity.get("verb"), String.valueOf(Verb.LISTEN));

        Map<String, Object> object = (Map<String, Object>) esActivity.get("object");
        Song song = (Song) activity.getActivity().getObject();
        assertEquals(object.size(), 5);
        assertEquals(object.get("mbid"), song.getMbid());
        assertEquals(object.get("url"), song.getUrl().toString());
        assertEquals(object.get("name"), "My Song");
        assertEquals(object.get("type"), Verb.SONG.name());
        assertNull(object.get("description"));

        Map<String, Object> context = (Map<String, Object>) esActivity.get("context");
        Context songContext = activity.getActivity().getContext();
        assertEquals(context.size(), 4);
        assertEquals(context.get("date"), songContext.getDate().getMillis());
        assertEquals(context.get("service"), lastFmServiceUrl);
        assertEquals(context.get("username"), "lastfm-username");
        assertNull(context.get("mood"));
    }

    private void assertHitsEqualLastFmActivities(
            SearchHits hits,
            UUID userId,
            Collection<ResolvedActivity> lastFmActivities
    ) {
        List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>(lastFmActivities);
        int numListens = activities.size();
        assertEquals(hits.getTotalHits(), numListens);

        for (int i = 0; i < numListens; i++) {
            SearchHit hit = hits.getAt(i);
            assertHitEqualsLastFmActivity(hit, userId, activities.get(i));
        }
    }

    private void delete(File file) throws IOException {
        if (!file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                delete(f);
            }
        }

        if (!file.delete()) {
            throw new IOException("Unable to delete file " + file + ".");
        }
    }
}
