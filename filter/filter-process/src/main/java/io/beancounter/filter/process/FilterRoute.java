package io.beancounter.filter.process;

import java.util.HashSet;
import java.util.Set;

import com.google.inject.Inject;

import io.beancounter.filter.FilterServiceException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.beancounter.commons.model.activity.ResolvedActivity;
import io.beancounter.filter.FilterService;

public class FilterRoute extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterRoute.class);

    private static final String ENDPOINTS_HEADER = "endpoints";

    @Inject
    private FilterService filterService;

    public void configure() {
        errorHandler(deadLetterChannel(errorEndpoint()));
        try {
            filterService.refresh();
        } catch (FilterServiceException e) {
            final String errMsg = "Error while refreshing filters from the manager";
            LOGGER.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }

        from(fromKestrel())
                .unmarshal().json(JsonLibrary.Jackson, ResolvedActivity.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        ResolvedActivity resolvedActivity = exchange.getIn().getBody(ResolvedActivity.class);
                        Set<String> targets = filterService.processActivity(resolvedActivity);
                        targets = appendTargetPrefix(targets);
                        exchange.getIn().setHeader(ENDPOINTS_HEADER, targets);
                    }
                })

                .marshal().json(JsonLibrary.Jackson, ResolvedActivity.class)

                .filter(header(ENDPOINTS_HEADER).isNotNull())

                .recipientList(header(ENDPOINTS_HEADER)).parallelProcessing().ignoreInvalidEndpoints();


        from(fromRedis())
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        String filterId = exchange.getIn().getBody(String.class);
                        LOGGER.debug("reloading filter [{}]", filterId);
                        filterService.refresh(filterId);
                    }
                });
    }

    protected Set<String> appendTargetPrefix(Set<String> targets) {
        if (targets != null) {
            Set<String> prefixedTargets = new HashSet<String>();
            for (String target : targets) {
                prefixedTargets.add("kestrel://{{kestrel.queue.filter.prefix.url}}" + target);
            }
            return prefixedTargets;
        }
        return null;
    }

    protected String fromKestrel() {
        return "kestrel://{{kestrel.queue.filter.url}}";
    }

    protected String errorEndpoint() {
        return "log:" + getClass().getSimpleName() + "?{{camel.log.options.error}}";
    }

    protected String fromRedis() {
        // TODO (low) this should be configurable and not embedded
        return "redis://localhost:6379?command=SUBSCRIBE&channels=filters&serializer=#serializer";
    }

}
