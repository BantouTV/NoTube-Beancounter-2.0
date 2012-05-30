package tv.notube.activities;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.joda.time.DateTime;
import tv.notube.activities.model.activity.ElasticSearchActivity;
import tv.notube.commons.model.activity.*;

import java.io.IOException;
import java.lang.Object;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * <a href="http://www.elasticsearch.org">ElasticSearch</a> based implementation
 * of {@link ActivityStore}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 * @author Alex Cowell ( alxcwll@gmail.com )
 */
public class ElasticSearchActivityStoreImpl implements ActivityStore {

    public static final String INDEX_NAME = "beancounter";
    public static final String INDEX_TYPE = "activity";

    private String hostname;
    private int port;
    private ObjectMapper mapper;

    public ElasticSearchActivityStoreImpl(String hostname, int port) {
        mapper = new ObjectMapper();

        this.hostname = hostname;
        this.port = port;
    }

    @Override
    public void store(UUID userId, Activity activity) throws ActivityStoreException {
        // TODO: Make it work over HTTP.
//        Client client = new TransportClient()
//                .addTransportAddress(new InetSocketTransportAddress(hostname, port));
        Node node = NodeBuilder.nodeBuilder().local(true).node();
        Client client = node.client();

        indexActivity(userId, activity, client);

        client.close();
    }

    @Override
    public void store(UUID userId, Collection<Activity> activities) throws ActivityStoreException {
        Node node = NodeBuilder.nodeBuilder().local(true).node();
        Client client = node.client();

        for (Activity activity : activities) {
            indexActivity(userId, activity, client);
        }

        client.close();
    }

    @Override
    public Collection<Activity> getByUser(UUID uuidId, int max) throws ActivityStoreException {
        Node node = NodeBuilder.nodeBuilder().local(true).node();
        Client client = node.client();

        SearchResponse searchResponse = client.prepareSearch(INDEX_NAME)
                .setQuery(queryString("userId:" + uuidId.toString()))
                .execute().actionGet();

        Collection<Activity> activities = new ArrayList<Activity>();

        for (SearchHit hit : searchResponse.getHits()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> activity =
                        (Map<String, Object>) hit.getSource().get("activity");

                activities.add(mapper.readValue(mapper.writeValueAsBytes(activity), Activity.class));
            } catch (IOException ioe) {
                // TODO
            }
        }

        client.close();

        return activities;
    }

    @Override
    public Collection<Activity> getByUserAndDateRange(UUID uuid, DateTime from, DateTime to) throws ActivityStoreException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<UUID, Collection<Activity>> getByDateRange(DateTime from, DateTime to) throws ActivityStoreException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private void indexActivity(UUID userId, Activity activity, Client client) {
        ElasticSearchActivity esa = new ElasticSearchActivity(userId, activity);

        try {
            client.prepareIndex(INDEX_NAME, INDEX_TYPE)
                    .setSource(createActivityJson(esa))
                    .execute().actionGet();
        } catch (IOException ioe) {
            // TODO.
        }
    }

    private byte[] createActivityJson(ElasticSearchActivity esa) throws IOException {
        return mapper.writeValueAsBytes(esa);
    }
}
