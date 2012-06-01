package tv.notube.activities;

import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import tv.notube.activities.model.activity.ElasticSearchActivity;
import tv.notube.commons.configuration.activities.ElasticSearchConfiguration;
import tv.notube.commons.configuration.activities.NodeInfo;
import tv.notube.commons.model.activity.*;

import java.io.IOException;
import java.lang.Object;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.elasticsearch.index.query.FilterBuilders.*;
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

    private ObjectMapper mapper;
    private ElasticSearchConfiguration configuration;
    private Client client;

    public ElasticSearchActivityStoreImpl(ElasticSearchConfiguration configuration) {
        this.configuration = configuration;
        client = getClient();
        mapper = new ObjectMapper();
    }

    @Override
    public void store(UUID userId, Activity activity) throws ActivityStoreException {
        indexActivity(userId, activity, client);
    }

    @Override
    public void store(UUID userId, Collection<Activity> activities) throws ActivityStoreException {
        for (Activity activity : activities) {
            indexActivity(userId, activity, client);
        }
    }

    @Override
    public Collection<Activity> getByUser(UUID uuidId, int max) throws ActivityStoreException {
        SearchResponse searchResponse = client.prepareSearch(INDEX_NAME)
                .setQuery(queryString("userId:" + uuidId.toString()))
                .addSort(INDEX_TYPE + ".activity.context.date", SortOrder.DESC)
                .setSize(max)
                .execute().actionGet();

        return retrieveActivitiesFromSearchResponse(searchResponse);
    }

    @Override
    public Collection<Activity> getByUserAndDateRange(UUID uuid, DateTime from, DateTime to)
            throws ActivityStoreException {
        SearchResponse searchResponse = client.prepareSearch(INDEX_NAME)
                .setQuery(queryString("userId:" + uuid.toString()))
                .addSort(INDEX_TYPE + ".activity.context.date", SortOrder.DESC)
                .setFilter(numericRangeFilter(INDEX_TYPE + ".activity.context.date")
                        .from(from.getMillis())
                        .to(to.getMillis())
                ).execute().actionGet();

        return retrieveActivitiesFromSearchResponse(searchResponse);
    }

    @Override
    public Map<UUID, Collection<Activity>> getByDateRange(DateTime from, DateTime to)
            throws ActivityStoreException {
        SearchResponse searchResponse = client.prepareSearch(INDEX_NAME)
                .setQuery(QueryBuilders.matchAllQuery())
                .addSort(INDEX_TYPE + ".activity.context.date", SortOrder.DESC)
                .setFilter(numericRangeFilter(INDEX_TYPE + ".activity.context.date")
                        .from(from.getMillis())
                        .to(to.getMillis())
                ).execute().actionGet();

        Map<UUID, Collection<Activity>> activitiesMap = new HashMap<UUID, Collection<Activity>>();

        // TODO: Use facets or some type of grouping to avoid populating the map
        // manually.
        for (SearchHit hit : searchResponse.getHits()) {
            try {
                UUID userId = UUID.fromString((String) hit.getSource().get("userId"));
                @SuppressWarnings("unchecked")
                Map<String, Object> activity =
                        (Map<String, Object>) hit.getSource().get("activity");

                if (activitiesMap.get(userId) == null) {
                    activitiesMap.put(userId, new ArrayList<Activity>());
                }
                List<Activity> activities = (List<Activity>) activitiesMap.get(userId);
                activities.add(mapper.readValue(mapper.writeValueAsBytes(activity), Activity.class));
            } catch (IOException ioe) {
                // TODO
            }
        }

        return activitiesMap;
    }

    public void closeClient() {
        client.close();
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

    private Collection<Activity> retrieveActivitiesFromSearchResponse(
            SearchResponse searchResponse
    ) {
        Collection<Activity> activities = new ArrayList<Activity>();

        for (SearchHit hit : searchResponse.getHits()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> activity =
                        (Map<String, Object>) hit.getSource().get("activity");

                activities.add(mapper.readValue(
                        mapper.writeValueAsBytes(activity),
                        Activity.class
                ));
            } catch (IOException ioe) {
                // TODO
            }
        }

        return activities;
    }

    private Client getClient() {
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("client.transport.sniff", true)
                .build();
        TransportClient client = new TransportClient(settings);

        for (NodeInfo node : configuration.getNodes()) {
            client.addTransportAddress(
                    new InetSocketTransportAddress(node.getHost(), node.getPort())
            );
        }

        ImmutableList<DiscoveryNode> nodes = client.connectedNodes();
        if (nodes.isEmpty()) {
            client.close();
            throw new RuntimeException("Could not connect to elasticsearch cluster."
                    + " Please check the elasticsearch-configuration.xml file.");
        }

        return client;
    }
}
