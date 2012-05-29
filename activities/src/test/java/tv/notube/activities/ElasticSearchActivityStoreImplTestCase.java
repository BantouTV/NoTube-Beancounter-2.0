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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static tv.notube.activities.ElasticSearchActivityStoreImpl.INDEX_NAME;
import static tv.notube.activities.ElasticSearchActivityStoreImpl.INDEX_TYPE;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
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
        ab.setContext(new DateTime(), new URL("http://twitter.com"));

        as.store(userId, ab.pop());

        // Refresh so we're looking at the latest version of the index.
        client.admin().indices().refresh(new RefreshRequest(INDEX_NAME)).actionGet();
        SearchResponse searchResponse = client.prepareSearch(INDEX_NAME)
                .setQuery(QueryBuilders.matchAllQuery())
                .execute().actionGet();

        SearchHits hits = searchResponse.getHits();
        assertEquals(hits.getTotalHits(), 1);
        assertEquals(hits.getAt(0).getType(), INDEX_TYPE);
        assertEquals(hits.getAt(0).getSource().get("userId").toString(), userId.toString());
        assertEquals(hits.getAt(0).getSource().get("setText").toString(), "This is a test tweet!");
    }

    @Test
    public void storeMultipleTweetsForAUser() throws Exception {
        clearIndices();

        UUID userId = UUID.randomUUID();

        int numTweets = 5;
        ActivityBuilder ab = new DefaultActivityBuilder();
        Collection<Activity> activities = new ArrayList<Activity>(numTweets);

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
            ab.setContext(new DateTime(), new URL("http://twitter.com"));
            activities.add(ab.pop());
        }

        as.store(userId, activities);

        // Refresh so we're looking at the latest version of the index.
        client.admin().indices().refresh(new RefreshRequest(INDEX_NAME)).actionGet();
        SearchResponse searchResponse = client.prepareSearch(INDEX_NAME)
                .setQuery(QueryBuilders.matchAllQuery())
                .execute().actionGet();
        assertEquals(searchResponse.getHits().getTotalHits(), 5);
    }

    @Test
    public void testCRUD() throws Exception {
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
