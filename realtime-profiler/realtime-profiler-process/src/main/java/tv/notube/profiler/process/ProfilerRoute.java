package tv.notube.profiler.process;

import com.google.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tv.notube.commons.model.UserProfile;
import tv.notube.commons.model.activity.ResolvedActivity;
import tv.notube.profiler.Profiler;

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
                        LOGGER.debug("Profiling activity {}.", resolvedActivity);
                        try {
                            UserProfile profile = profiler.profile(
                                    resolvedActivity.getUserId(),
                                    resolvedActivity.getActivity()
                            );
                        } catch (Exception e) {
                            // log the error but do not raise an exception
                            final String errMsg = "Error while profiling user [" + resolvedActivity
                                    .getUserId() + "]";
                            LOGGER.error(errMsg, e);
                        }
                        // (TODO) (low) profile will be sent in a down stream queue
                        // meant to persist all the profiles of every user
                        // and yes, even to other real-time processes
                        // exchange.getIn().setBody(profile);
                    }
                }
                );
        // TODO (out of release 1.0) turn on profiling analytics
        /**
         .marshal().json(JsonLibrary.Jackson)
         .convertBodyTo(String.class)
         .to("kestrel://{{kestrel.queue.analytics}}");
         **/

    }

    protected String fromKestrel() {
        return "kestrel://{{kestrel.queue.profiler.url}}?concurrentConsumers=10&waitTimeMs=500";
    }

    protected String errorEndpoint() {
        return "log:indexerRoute?level=ERROR";
    }


}