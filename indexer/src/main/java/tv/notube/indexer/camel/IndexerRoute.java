package tv.notube.indexer.camel;

import java.util.Properties;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tv.notube.activities.ActivityStore;
import tv.notube.activities.ElasticSearchActivityStoreFactory;
import tv.notube.commons.lupedia.LUpediaNLPEngineImpl;
import tv.notube.commons.model.UserProfile;
import tv.notube.commons.model.activity.ResolvedActivity;
import tv.notube.commons.model.activity.Tweet;
import tv.notube.indexer.ActivityServiceImpl;
import tv.notube.profiler.DefaultProfilerImpl;
import tv.notube.profiler.Profiler;
import tv.notube.profiler.ProfilerException;
import tv.notube.profiler.rules.custom.TweetProfilingRule;
import tv.notube.profiles.Profiles;
import tv.notube.profiles.ProfilesModule;

/**
 *
 */
public class IndexerRoute extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexerRoute.class);

    private final ActivityServiceImpl activityService;

    private final Profiler profiler;

    public IndexerRoute(Properties properties) {
        ActivityStore activityStore = ElasticSearchActivityStoreFactory.getInstance().build();
        activityService = new ActivityServiceImpl(activityStore);
        try {
            profiler = createProfiler(properties);
        } catch (ProfilerException e) {
            throw new RuntimeException(
                    "Error while creating the Profiler",
                    e
            );
        }
    }

    public void configure() {
        from("kestrel://{{kestrel.queue.internal.url}}?concurrentConsumers=10&waitTimeMs=500")
                .unmarshal().json(JsonLibrary.Jackson, ResolvedActivity.class)
                .multicast().parallelProcessing().to("direct:es", "direct:profiler");
        from("direct:es")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        ResolvedActivity resolvedActivity = exchange.getIn()
                                .getBody(ResolvedActivity.class);
                        activityService.store(
                                resolvedActivity.getUserId(),
                                resolvedActivity.getActivity()
                        );
                    }
                });
        from("direct:profiler")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        ResolvedActivity resolvedActivity = exchange.getIn().getBody(ResolvedActivity.class);
                        LOGGER.debug("Profiling activity {}.", resolvedActivity);
                        UserProfile profile = profiler.profile(
                                resolvedActivity.getUserId(),
                                resolvedActivity.getActivity()
                        );
                    }
                });
    }

    private Profiler createProfiler(Properties properties) throws ProfilerException {
        Injector injector = Guice.createInjector(new ProfilesModule());
        Profiles profiles = injector.getInstance(Profiles.class);
        Profiler profiler = new DefaultProfilerImpl(
                profiles,
                new LUpediaNLPEngineImpl(),
                null,
                properties
        );
        profiler.registerRule(Tweet.class, TweetProfilingRule.class);
        return profiler;
    }
}
