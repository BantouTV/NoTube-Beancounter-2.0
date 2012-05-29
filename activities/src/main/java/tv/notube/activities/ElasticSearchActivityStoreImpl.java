package tv.notube.activities;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.joda.time.DateTime;
import tv.notube.commons.model.activity.Activity;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * <a href="http://www.elasticsearch.org">ElasticSearch</a> based implementation
 * of {@link ActivityStore}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ElasticSearchActivityStoreImpl implements ActivityStore {

    public static final String INDEX_NAME = "beancounter";
    public static final String INDEX_TYPE = "activity";

    private String hostname;
    private int port;

    public ElasticSearchActivityStoreImpl(String hostname, int port) {
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

        try {
            client.prepareIndex(INDEX_NAME, INDEX_TYPE)
                    .setSource(XContentFactory.jsonBuilder()
                            .startObject()
                                .field("userId", userId)
                            .endObject()
                    )
            .execute().actionGet();
        } catch (IOException ioe) {
            // TODO.
        }

        client.close();
    }

    @Override
    public void store(UUID userId, Collection<Activity> activities) throws ActivityStoreException {
        Node node = NodeBuilder.nodeBuilder().local(true).node();
        Client client = node.client();

        for (Activity activity : activities) {
            try {
                client.prepareIndex(INDEX_NAME, INDEX_TYPE)
                        .setSource(XContentFactory.jsonBuilder()
                                .startObject()
                                .field("userId", userId)
                                .endObject()
                        )
                        .execute().actionGet();
            } catch (IOException ioe) {
                // TODO.
            }
        }

        client.close();
    }

    @Override
    public Collection<Activity> getByUser(UUID uuidId, int max) throws ActivityStoreException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<Activity> getByUserAndDateRange(UUID uuid, DateTime from, DateTime to) throws ActivityStoreException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<UUID, Collection<Activity>> getByDateRange(DateTime from, DateTime to) throws ActivityStoreException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
