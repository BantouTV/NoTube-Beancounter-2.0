package tv.notube.activities;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
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
import tv.notube.commons.model.activity.DefaultActivityBuilder;
import tv.notube.commons.model.activity.Tweet;
import tv.notube.commons.model.activity.Verb;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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

    @BeforeClass
    public void beforeClass() throws Exception {
        node = NodeBuilder.nodeBuilder().local(true).node();
        client = node.client();

        try {
            // The default index will be created with 5 shards with 1 replica
            // per shard.
            client.admin().indices().create(new CreateIndexRequest(INDEX_NAME)).actionGet();
        } catch (ElasticSearchException indexAlreadyExists) {
            clearIndices();
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
    @SuppressWarnings("unchecked")
    public void storeASingleTweetForAUser() throws Exception {
        clearIndices();

        UUID userId = UUID.randomUUID();
        ActivityBuilder ab = new DefaultActivityBuilder();

        ab.push();
        ab.setVerb(Verb.TWEET);
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("setText", "This is a test tweet!");
        ab.setObject(
                Tweet.class,
                new URL("http://twitter.com/#!/test-user/status/175538466216611841"),
                "Test",
                fields
        );
        DateTime dateTime = new DateTime();
        String serviceUrl = "http://twitter.com";
        ab.setContext(dateTime, new URL(serviceUrl));

        as.store(userId, ab.pop());

        // Refresh so we're looking at the latest version of the index.
        client.admin().indices().refresh(new RefreshRequest(INDEX_NAME)).actionGet();
        SearchResponse searchResponse = client.prepareSearch(INDEX_NAME)
                .setQuery(QueryBuilders.matchAllQuery())
                .execute().actionGet();

        SearchHits hits = searchResponse.getHits();
        assertEquals(hits.getTotalHits(), 1);

        SearchHit hit = hits.getAt(0);
        assertEquals(hit.getType(), INDEX_TYPE);

        Map<String, Object> source = hit.getSource();
        assertEquals(source.get("userId"), userId.toString());

        Map<String, Object> activity = (Map<String, Object>) source.get("activity");
        assertEquals(activity.get("verb"), String.valueOf(Verb.TWEET));

        Map<String, Object> object = (Map<String, Object>) activity.get("object");
        assertEquals(object.size(), 7);
        assertEquals(object.get("text"), "This is a test tweet!");
        assertEquals(object.get("url"), "http://twitter.com/#!/test-user/status/175538466216611841");
        assertEquals(object.get("hashTags"), Collections.emptyList());
        assertEquals(object.get("urls"), Collections.emptyList());
        assertEquals(object.get("name"), "Test");
        assertEquals(object.get("@class"), Tweet.class.getName());
        assertNull(object.get("description"));

        Map<String, Object> context = (Map<String, Object>) activity.get("context");
        assertEquals(context.size(), 3);
        assertEquals(context.get("date"), dateTime.getMillis());
        assertEquals(context.get("service"), serviceUrl);
        assertNull(context.get("mood"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void storeMultipleTweetsForAUser() throws Exception {
        clearIndices();

        UUID userId = UUID.randomUUID();

        int numTweets = 5;
        ActivityBuilder ab = new DefaultActivityBuilder();
        Collection<Activity> activities = new ArrayList<Activity>(numTweets);

        DateTime dateTime = new DateTime();
        String serviceUrl = "http://twitter.com";

        for (int i = 0; i < numTweets; i++) {
            ab.push();
            ab.setVerb(Verb.TWEET);
            Map<String, Object> fields = new HashMap<String, Object>();
            fields.put("setText", "This is test tweet number " + i + "!");
            ab.setObject(
                    Tweet.class,
                    new URL("http://twitter.com/#!/test-user/status/17553846621661184" + i),
                    "Test",
                    fields
            );
            ab.setContext(dateTime, new URL(serviceUrl));
            activities.add(ab.pop());
        }

        as.store(userId, activities);

        // Refresh so we're looking at the latest version of the index.
        client.admin().indices().refresh(new RefreshRequest(INDEX_NAME)).actionGet();
        SearchResponse searchResponse = client.prepareSearch(INDEX_NAME)
                .setQuery(QueryBuilders.matchAllQuery())
                .execute().actionGet();

        SearchHits hits = searchResponse.getHits();
        assertEquals(hits.getTotalHits(), numTweets);

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
            assertEquals(object.get("text"), "This is test tweet number " + tweetNumber + "!");
            assertEquals(url, "http://twitter.com/#!/test-user/status/17553846621661184" + tweetNumber);
            assertEquals(object.get("hashTags"), Collections.emptyList());
            assertEquals(object.get("urls"), Collections.emptyList());
            assertEquals(object.get("name"), "Test");
            assertEquals(object.get("@class"), Tweet.class.getName());
            assertNull(object.get("description"));

            Map<String, Object> context = (Map<String, Object>) activity.get("context");
            assertEquals(context.size(), 3);
            assertEquals(context.get("date"), dateTime.getMillis());
            assertEquals(context.get("service"), serviceUrl);
            assertNull(context.get("mood"));
        }
    }

    @Test
    public void getLatestTweetByAUserGivenOneUserAndTheyHaveOneTweet() throws Exception {
        clearIndices();

        UUID userId = UUID.randomUUID();
        ActivityBuilder ab = new DefaultActivityBuilder();

        ab.push();
        ab.setVerb(Verb.TWEET);
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("setText", "This is a test tweet!");
        ab.setObject(
                Tweet.class,
                new URL("http://twitter.com/#!/test-user/status/175538466216611841"),
                "Test",
                fields
        );
        DateTime dateTime = new DateTime();
        String serviceUrl = "http://twitter.com";
        ab.setContext(dateTime, new URL(serviceUrl));

        as.store(userId, ab.pop());

        // Refresh to ensure the addition was committed.
        client.admin().indices().refresh(new RefreshRequest(INDEX_NAME)).actionGet();

        Collection<Activity> activities = as.getByUser(userId, 1);
        assertEquals(activities.size(), 1);
    }

    @Test
    public void getLatestTwoTweetsByAUserGivenOneUserAndTheyHaveTwoTweets() throws Exception {
        clearIndices();

        UUID userId = UUID.randomUUID();
        int numTweets = 2;
        ActivityBuilder ab = new DefaultActivityBuilder();
        Collection<Activity> activities = new ArrayList<Activity>(numTweets);

        String serviceUrl = "http://twitter.com";

        for (int i = 0; i < numTweets; i++) {
            ab.push();
            ab.setVerb(Verb.TWEET);
            Map<String, Object> fields = new HashMap<String, Object>();
            fields.put("setText", "This is test tweet number " + i + "!");
            ab.setObject(
                    Tweet.class,
                    new URL("http://twitter.com/#!/test-user/status/17553846621661184" + i),
                    "Test",
                    fields
            );
            DateTime dateTime = new DateTime();
            ab.setContext(dateTime.minusDays(i), new URL(serviceUrl));
            activities.add(ab.pop());
        }

        as.store(userId, activities);

        // Refresh so we're looking at the latest version of the index.
        client.admin().indices().refresh(new RefreshRequest(INDEX_NAME)).actionGet();

        activities = as.getByUser(userId, 2);
        assertEquals(activities.size(), 2);
    }

    @Test
    public void getLatestTweetByAUserGivenUserHasOneTweetAndTwoTotalUsers() throws Exception {
        clearIndices();

        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        int numTweets = 1;
        ActivityBuilder ab = new DefaultActivityBuilder();
        Collection<Activity> activities = new ArrayList<Activity>(numTweets);

        String serviceUrl = "http://twitter.com";

        for (int i = 0; i < numTweets; i++) {
            ab.push();
            ab.setVerb(Verb.TWEET);
            Map<String, Object> fields = new HashMap<String, Object>();
            fields.put("setText", "This is test tweet number " + i + " from user " + userId1 + "!");
            ab.setObject(
                    Tweet.class,
                    new URL("http://twitter.com/#!/test-user/status/17553846621661184" + i),
                    "Test",
                    fields
            );
            DateTime dateTime = new DateTime();
            ab.setContext(dateTime.minusDays(i), new URL(serviceUrl));
            activities.add(ab.pop());
        }

        as.store(userId1, activities);
        activities.clear();

        for (int i = 0; i < numTweets; i++) {
            ab.push();
            ab.setVerb(Verb.TWEET);
            Map<String, Object> fields = new HashMap<String, Object>();
            fields.put("setText", "This is test tweet number " + i + " from user " + userId2+ "!");
            ab.setObject(
                    Tweet.class,
                    new URL("http://twitter.com/#!/test-user/status/17553846621661184" + i),
                    "Test",
                    fields
            );
            DateTime dateTime = new DateTime();
            ab.setContext(dateTime.minusDays(i), new URL(serviceUrl));
            activities.add(ab.pop());
        }

        as.store(userId2, activities);

        // Refresh so we're looking at the latest version of the index.
        client.admin().indices().refresh(new RefreshRequest(INDEX_NAME)).actionGet();

        activities = as.getByUser(userId1, 1);
        assertEquals(activities.size(), 1);
    }

    @Test
    public void testCRUD() throws Exception {
        // TODO
        final UUID userId = UUID.randomUUID();
    }

    private void clearIndices() throws Exception {
        client.prepareDeleteByQuery(INDEX_NAME)
                .setQuery(QueryBuilders.matchAllQuery())
                .execute().actionGet();

        // Wait for shards to settle (return to idle state).
        client.admin().indices().refresh(new RefreshRequest(INDEX_NAME)).actionGet();
        client.admin().cluster().health(new ClusterHealthRequest(INDEX_NAME).waitForYellowStatus()).actionGet();
    }
}
