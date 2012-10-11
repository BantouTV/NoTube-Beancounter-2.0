package io.beancounter.profiler.process;

import com.google.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.beancounter.commons.model.UserProfile;
import io.beancounter.commons.model.activity.ResolvedActivity;
import io.beancounter.profiler.Profiler;

public class ProfilerRoute extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfilerRoute.class);

    @Inject
    private Profiler profiler;

    public void configure() {
        errorHandler(deadLetterChannel(errorEndpoint()));

        from(fromKestrel())
                .unmarshal().json(JsonLibrary.Jackson, ResolvedActivity.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        ResolvedActivity resolvedActivity = exchange.getIn().getBody(ResolvedActivity.class);
                        LOGGER.debug("profiling activity: {}.", resolvedActivity);
                        UserProfile profile;
                        try {
                            profile = profiler.profile(
                                    resolvedActivity.getUserId(),
                                    resolvedActivity.getActivity()
                            );
                            exchange.getIn().setBody(profile);
                        } catch (Exception e) {
                            // log the error but do not raise an exception
                            final String errMsg = "error while profiling user [" + resolvedActivity.getUserId() + "]";
                            LOGGER.warn(errMsg, e);
                            exchange.getIn().setBody(null);
                        }
                    }
                })
                .filter(body().isNotNull())
                .marshal().json(JsonLibrary.Jackson)
                .convertBodyTo(String.class)
                .to(toKestrel());
    }

    protected String toKestrel() {
        return "kestrel://{{kestrel.queue.profiles.url}}";
    }

    protected String fromKestrel() {
        return "kestrel://{{kestrel.queue.profiler.url}}?concurrentConsumers=10&waitTimeMs=500";
    }

    protected String errorEndpoint() {
        return "log:" + getClass().getSimpleName() + "?{{camel.log.options.error}}";
    }


}
