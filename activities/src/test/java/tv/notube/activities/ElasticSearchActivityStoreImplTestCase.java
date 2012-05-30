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
import org.joda.time.DateTime;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static tv.notube.activities.ElasticSearchActivityStoreImpl.INDEX_NAME;
import static tv.notube.activities.ElasticSearchActivityStoreImpl.INDEX_TYPE;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 * @author Alex Cowell ( alxcwll@gmail.com )
 */
public class ElasticSearchActivityStoreImplTestCase {

    private ActivityStore as;

    private Node node;
    private Client client;
    private String tweetServiceUrl = "http://twitter.com";

    @BeforeClass
    public void beforeClass() throws Exception {
        node = NodeBuilder.nodeBuilder().local(true).node();
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

    @AfterClass
    public void afterClass() throws Exception {
        node.close();

        // TODO: Remove the ES data dir
    }

    @BeforeTest
    public void setUp() throws Exception {
        as = new ElasticSearchActivityStoreImpl("localhost", 9200);
    }

    @AfterTest
    public void tearDown() throws Exception {
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
    @SuppressWarnings("unchecked")
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
                .execute().actionGet();

        SearchHits hits = searchResponse.getHits();
        assertEquals(hits.getTotalHits(), numTweets);

        //assertHitsEqualTweetActivities(hits, userId, tweetActivities);

        for (int i = 0; i < numTweets; i++) {
            SearchHit hit = hits.getAt(i);
            assertEquals(hit.getType(), INDEX_TYPE);

            Map<String, Object> source = hit.getSource();
            assertEquals(source.get("userId"), userId.toString());

            Map<String, Object> activity = (Map<String, Object>) source.get("activity");
            assertEquals(activity.get("verb"), String.valueOf(Verb.TWEET));

            Map<String, Object> object = (Map<String, Object>) activity.get("object");
            String url = (String) object.get("url");
            int tweetNumber = Integer.valueOf(url.substring(url.length() - 1), 10);
            assertEquals(object.size(), 7);
            assertEquals(object.get("text"), "This is test tweet number "
                    + tweetNumber + " from user " + userId.toString() + "!");
            assertEquals(url, "http://twitter.com/#!/test-user/status/" + tweetNumber);
            assertEquals(object.get("hashTags"), Collections.emptyList());
            assertEquals(object.get("urls"), Collections.emptyList());
            assertEquals(object.get("name"), "Test");
            assertEquals(object.get("@class"), Tweet.class.getName());
            assertNull(object.get("description"));

            Map<String, Object> context = (Map<String, Object>) activity.get("context");
            assertEquals(context.size(), 3);
            assertEquals(context.get("date"), dateTime.minusDays(tweetNumber).getMillis());
            assertEquals(context.get("service"), tweetServiceUrl);
            assertNull(context.get("mood"));
        }
    }

    private void assertHitsEqualTweetActivities(SearchHits hits, UUID userId, List<Activity> tweetActivities) {
        int numTweets = tweetActivities.size();
        assertEquals(hits.getTotalHits(), numTweets);

        for (int i = 0; i < numTweets; i++) {
            SearchHit hit = hits.getAt(i);
            assertHitEqualsTweetActivity(hit, userId, tweetActivities.get(i));
        }
    }

    @Test
    public void getLatestTweetByAUserGivenOneUserAndTheyHaveOneTweet() throws Exception {
        clearIndex();

        UUID userId = UUID.randomUUID();

        as.store(userId, createTweetActivity(0, userId, new DateTime()));

        // Refresh to ensure the addition was committed.
        refreshIndex();

        Collection<Activity> activities = as.getByUser(userId, 1);
        assertEquals(activities.size(), 1);
    }

    @Test
    public void getLatestTwoTweetsByAUserGivenOneUserAndTheyHaveTwoTweets() throws Exception {
        clearIndex();

        UUID userId = UUID.randomUUID();
        int numTweets = 2;

        as.store(userId, createTweetActivities(userId, new DateTime(), numTweets));

        refreshIndex();

        Collection<Activity> activities = as.getByUser(userId, 2);
        assertEquals(activities.size(), numTweets);
    }

    @Test
    public void getLatestTweetByAUserGivenUserHasOneTweetAndTwoTotalUsers() throws Exception {
        clearIndex();

        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        as.store(userId1, createTweetActivity(0, userId1, new DateTime()));
        as.store(userId2, createTweetActivity(1, userId2, new DateTime()));

        refreshIndex();

        Collection<Activity> activities = as.getByUser(userId1, 1);
        assertEquals(activities.size(), 1);
    }

    @Test
    public void getTweetsOfUserSpecifiedByDateRange() throws Exception {
        clearIndex();

        UUID userId = UUID.randomUUID();
        DateTime dateTime = new DateTime();

        as.store(userId, createTweetActivities(userId, dateTime, 10));

        refreshIndex();

        Collection<Activity> userActivities = as.getByUser(userId, 10);
        Collection<Activity> activities = as.getByUserAndDateRange(userId, new DateTime().minusDays(5), new DateTime());
        assertEquals(userActivities.size(), 10);
        assertEquals(activities.size(), 5);
    }

    @Test
    public void getTweetsOfAllUsersSpecifiedByDateRange() throws Exception {
        clearIndex();

        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        DateTime dateTime = new DateTime();

        as.store(userId1, createTweetActivities(userId1, dateTime, 10));
        as.store(userId2, createTweetActivities(userId2, dateTime, 10));

        refreshIndex();

        Map<UUID, Collection<Activity>> allActivity = as.getByDateRange(new DateTime().minusDays(5), new DateTime());
        assertEquals(allActivity.size(), 2);

        Collection<Activity> activities = allActivity.get(userId1);
        assertEquals(activities.size(), 5);

        activities = allActivity.get(userId2);
        assertEquals(activities.size(), 5);
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
        ab.setContext(dateTime.minusDays(tweetId), new URL(tweetServiceUrl));

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
    private void assertHitEqualsTweetActivity(
            SearchHit hit,
            UUID userId, List<Object> hashTags, List<Object> urls,
            DateTime dateTime
    ) throws Exception {
        assertEquals(hit.getType(), INDEX_TYPE);

        Map<String, Object> source = hit.getSource();
        assertEquals(source.get("userId"), userId.toString());

        Map<String, Object> activity = (Map<String, Object>) source.get("activity");
        assertEquals(activity.get("verb"), String.valueOf(Verb.TWEET));

        Map<String, Object> object = (Map<String, Object>) activity.get("object");
        assertEquals(object.size(), 7);
        assertEquals(object.get("text"), "This is test tweet number 0 from user " + userId.toString() + "!");
        assertEquals(object.get("url"), "http://twitter.com/#!/test-user/status/0");
        assertEquals(object.get("hashTags"), hashTags);
        assertEquals(object.get("urls"), urls);
        assertEquals(object.get("name"), "Test");
        assertEquals(object.get("@class"), Tweet.class.getName());
        assertNull(object.get("description"));

        Map<String, Object> context = (Map<String, Object>) activity.get("context");
        assertEquals(context.size(), 3);
        assertEquals(context.get("date"), dateTime.getMillis());
        assertEquals(context.get("service"), tweetServiceUrl);
        assertNull(context.get("mood"));
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
}
