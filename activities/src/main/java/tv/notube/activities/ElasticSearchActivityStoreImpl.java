package tv.notube.activities;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import tv.notube.activities.model.activity.ElasticSearchActivity;
import tv.notube.commons.helper.es.ElasticSearchConfiguration;
import tv.notube.commons.helper.es.NodeInfo;
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
 * <a href="http://www.elasticsearch.org">ElasticSearch</a>
 * based implementation of {@link ActivityStore}.
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

    @Inject
    public ElasticSearchActivityStoreImpl(
            @Named("esConfiguration") ElasticSearchConfiguration configuration
    ) {
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
        // TODO (low): Use the Bulk API for this.
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
            UUID userId = UUID.fromString((String) hit.getSource().get("userId"));
            @SuppressWarnings("unchecked")
            Map<String, Object> activity =
                    (Map<String, Object>) hit.getSource().get("activity");
            if (activitiesMap.get(userId) == null) {
                activitiesMap.put(userId, new ArrayList<Activity>());
            }
            List<Activity> activities = (List<Activity>) activitiesMap.get(userId);
            byte[] bytes;
            try {
                bytes = mapper.writeValueAsBytes(activity);
            } catch (IOException e) {
                final String errMsg = "Error while serializing as bytes [" + activity + "]";
                throw new ActivityStoreException(errMsg, e);
            }
            Activity activityObj;
            try {
                activityObj = mapper.readValue(bytes, Activity.class);
            } catch (IOException ioe) {
                final String errMsg = "Error while deserializing [" + activity + "]";
                throw new ActivityStoreException(errMsg, ioe);
            }
            activities.add(activityObj);
        }
        return activitiesMap;
    }

    // TODO (med): Surely this method should be getActivity(activityId) since
    // we don't really care about the user?
    @Override
    public Activity getByUser(UUID userId, UUID activityId) throws ActivityStoreException {
        SearchResponse searchResponse = client.prepareSearch(INDEX_NAME)
                .setQuery(queryString("activity.id:" + activityId.toString()))
                .setSize(1)
                .execute().actionGet();

        List<Activity> activities =
                (List<Activity>) retrieveActivitiesFromSearchResponse(searchResponse);

        return activities.size() > 0 ? activities.get(0) : null;
    }

    @Override
    public Collection<Activity> getByUser(UUID userId, Collection<UUID> activityIds)
            throws ActivityStoreException {

        OrFilterBuilder orFilterBuilder = orFilter();
        for (UUID id : activityIds) {
            orFilterBuilder.add(queryFilter(queryString("activity.id:" + id.toString())));
        }

        FilteredQueryBuilder fqBuilder = filteredQuery(
                queryString("userId:" + userId.toString()),
                orFilterBuilder
        );

        SearchResponse searchResponse = client.prepareSearch(INDEX_NAME)
                .setQuery(fqBuilder)
                .addSort(INDEX_TYPE + ".activity.context.date", SortOrder.DESC)
                .execute().actionGet();

        return retrieveActivitiesFromSearchResponse(searchResponse);
    }

    @Override
    public Collection<Activity> getByUserPaginated(
            UUID userId, int pageNumber, int size
    ) throws ActivityStoreException {
        return searchAndPaginateResults("userId:" + userId.toString(), pageNumber, size);
    }

    @Override
    public Collection<Activity> search(
            String path, String value, int pageNumber, int size
    ) throws ActivityStoreException, WildcardSearchException {
        if (path.contains("*") || value.contains("*")) {
            throw new WildcardSearchException("Wildcard searches are not allowed.");
        }

        return searchAndPaginateResults(path + ":" + value, pageNumber, size);
    }

    @Override
    public void shutDown() throws ActivityStoreException {
        try {
            client.close();
        } catch (Exception e) {
            final String errMsg = "Error while closing the client";
            throw new ActivityStoreException(errMsg, e);
        }
    }

    private void indexActivity(UUID userId, Activity activity, Client client)
            throws ActivityStoreException {
        ElasticSearchActivity esa = new ElasticSearchActivity(userId, activity);
        byte[] jsonActivity;
        try {
            jsonActivity = createActivityJson(esa);
        } catch (IOException e) {
            final String errMsg = "Error while serializing to json [" + esa + "]";
            throw new ActivityStoreException(errMsg, e);
        }
        client.prepareIndex(INDEX_NAME, INDEX_TYPE)
                .setSource(jsonActivity)
                .execute().actionGet();
    }

    private byte[] createActivityJson(ElasticSearchActivity esa) throws IOException {
        return mapper.writeValueAsBytes(esa);
    }

    private Collection<Activity> searchAndPaginateResults(
            String query, int pageNumber, int size
    ) throws ActivityStoreException {
        SearchResponse searchResponse = client.prepareSearch(INDEX_NAME)
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setQuery(queryString(query))
                .addSort(INDEX_TYPE + ".activity.context.date", SortOrder.DESC)
                .setFrom(pageNumber * size)
                .setSize(size)
                .execute().actionGet();

        return retrieveActivitiesFromSearchResponse(searchResponse);
    }

    private Collection<Activity> retrieveActivitiesFromSearchResponse(
            SearchResponse searchResponse
    ) throws ActivityStoreException {
        Collection<Activity> activities = new ArrayList<Activity>();
        for (SearchHit hit : searchResponse.getHits()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> activity =
                    (Map<String, Object>) hit.getSource().get("activity");
            byte[] jsonActivity;
            try {
                jsonActivity = mapper.writeValueAsBytes(activity);
            } catch (IOException e) {
                final String errMsg = "Error while serializing to json [" + activity + "]";
                throw new ActivityStoreException(errMsg, e);
            }
            Activity activityObj;
            try {
                activityObj = mapper.readValue(
                        jsonActivity,
                        Activity.class
                );
            } catch (IOException e) {
                final String errMsg = "Error while serializing to json [" + activity + "]";
                throw new ActivityStoreException(errMsg, e);
            }
            activities.add(activityObj);
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
