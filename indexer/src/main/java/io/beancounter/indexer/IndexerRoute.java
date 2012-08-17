package io.beancounter.indexer;

import com.google.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.beancounter.activities.ActivityStore;
import io.beancounter.activities.ActivityStoreException;
import io.beancounter.commons.model.activity.ResolvedActivity;

public class IndexerRoute extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexerRoute.class);

    @Inject
    private ActivityStore activityStore;

    public void configure() {
        errorHandler(deadLetterChannel(errorEndpoint()));

        from(fromKestrel())

                .to(statisticsEndpoint())

                .unmarshal().json(JsonLibrary.Jackson, ResolvedActivity.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        ResolvedActivity resolvedActivity = exchange.getIn().getBody(ResolvedActivity.class);
                        try {
                            activityStore.store(resolvedActivity.getUserId(), resolvedActivity);
                        } catch (Exception e) {
                            final String errMsg = "Error while storing " + "resolved activity for user ["
                                    + resolvedActivity.getUserId() + "]";
                            LOGGER.error(errMsg, e);
                            throw new ActivityStoreException(errMsg, e);
                        }
                    }
                });
    }

    protected String statisticsEndpoint() {
        return "log:indexerRouteStatistics?{{camel.log.options.statistics}}";
    }

    protected String fromKestrel() {
        return "kestrel://{{kestrel.queue.indexer.url}}?concurrentConsumers=10&waitTimeMs=500";
    }

    protected String errorEndpoint() {
        return "log:" + getClass().getSimpleName() + "?{{camel.log.options.error}}";
    }
}
