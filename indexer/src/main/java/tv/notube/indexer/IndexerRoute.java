package tv.notube.indexer;

import com.google.inject.Inject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tv.notube.activities.ActivityStore;
import tv.notube.activities.ActivityStoreException;
import tv.notube.commons.model.UserProfile;
import tv.notube.commons.model.activity.ResolvedActivity;
import tv.notube.profiler.Profiler;
import tv.notube.profiler.ProfilerException;

/**
 *
 */
public class IndexerRoute extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexerRoute.class);

    @Inject
    private ActivityStore activityStore;

    @Inject
    private Profiler profiler;

    public void configure() {
        onException(ProfilerException.class).handled(true).process(
                new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        log.error("exception detected.");
                    }
                }
        );
        from("kestrel://{{kestrel.queue.internal.url}}?concurrentConsumers=10&waitTimeMs=500")
                .unmarshal().json(JsonLibrary.Jackson, ResolvedActivity.class)
                .multicast().parallelProcessing().to("direct:es", "direct:profiler");
        from("direct:es")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        ResolvedActivity resolvedActivity = exchange.getIn().getBody(ResolvedActivity.class);
                        try {
                            activityStore.store(
                                    resolvedActivity.getUserId(),
                                    resolvedActivity.getActivity()
                            );
                        } catch (ActivityStoreException e) {
                            final String errMsg = "Error while storing " + "resolved activity for user [" + resolvedActivity.getUserId() + "]";
                            LOGGER.error(errMsg, e);
                            throw new ActivityStoreException(errMsg, e);
                        }
                    }
                });
        from("direct:profiler")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        ResolvedActivity resolvedActivity = exchange.getIn().getBody(ResolvedActivity.class);
                        LOGGER.debug("Profiling activity {}.", resolvedActivity);
                        UserProfile profile;
                        // the profiler automatically stores the profile
                        //try {
                            profile = profiler.profile(
                                    resolvedActivity.getUserId(),
                                    resolvedActivity.getActivity()
                            );
                        //} catch (ProfilerException e) {
                            // log the error but do not raise an exception
                            // final String errMsg = "Error while profiling " +
                            //        "user [" + resolvedActivity.getUserId()
                            //        + "]";
                            //LOGGER.error(errMsg, e);
                        //}
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
}
