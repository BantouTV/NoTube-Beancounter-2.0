package tv.notube.indexer;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.testng.CamelTestSupport;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import tv.notube.activities.ActivityStore;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.ResolvedActivity;
import tv.notube.commons.model.randomisers.VerbRandomizer;
import tv.notube.commons.tests.TestsBuilder;
import tv.notube.commons.tests.TestsException;
import tv.notube.filter.FilterService;
import tv.notube.profiler.Profiler;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IndexerRouteTest extends CamelTestSupport {
    private Injector injector;
    private Profiler profiler;
    private ActivityStore activityStore;
    private FilterService filterService;

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return injector.getInstance(IndexerRoute.class);
    }

    @BeforeMethod
    public void setUp() throws Exception {

        injector = Guice.createInjector(new Module() {

            @Override
            public void configure(Binder binder) {
                activityStore = mock(ActivityStore.class);
                profiler = mock(Profiler.class);
                filterService = mock(FilterService.class);
                binder.bind(ActivityStore.class).toInstance(activityStore);
                binder.bind(Profiler.class).toInstance(profiler);
                binder.bind(FilterService.class).toInstance(filterService);

                binder.bind(IndexerRoute.class).toInstance(new IndexerRoute() {
                    @Override
                    public String fromKestrel() {
                        return "direct:start";
                    }

                    @Override
                    public String errorEndpoint() {
                        return "mock:error";
                    }

                    @Override
                    protected Set<String> appendTargetPrefix(Set<String> targets) {
                        HashSet<String> newTargets = new HashSet<String>();
                        newTargets.add("mock:custom");
                        return newTargets;
                    }
                });
            }
        });

        super.setUp();
    }


    @Test
    public void activityReachesBothServices() throws Exception {
        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(0);

        String json = activityAsJson();

        template.sendBody("direct:start", json);

        error.assertIsSatisfied();
        verify(activityStore).store(any(UUID.class), any(Activity.class));
        verify(profiler).profile(any(UUID.class), any(Activity.class));
        verify(filterService).processActivity(any(ResolvedActivity.class));
    }

    @Test
    public void logsErrorFromOneOfTheServices() throws Exception {
        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(1);

        doThrow(new RuntimeException("problem")).when(activityStore)
                .store(any(UUID.class), any(Activity.class));
        template.sendBody("direct:start", activityAsJson());

        error.assertIsSatisfied();
        verify(activityStore).store(any(UUID.class), any(Activity.class));
        verify(profiler).profile(any(UUID.class), any(Activity.class));
        verify(filterService).processActivity(any(ResolvedActivity.class));
    }

    @Test
    public void activityReachesCustomEndpoint() throws Exception {
        MockEndpoint custom = getMockEndpoint("mock:custom");
        custom.expectedMessageCount(1);

        String json = activityAsJson();
        when(filterService.processActivity(any(ResolvedActivity.class))).thenReturn(Collections.<String>emptySet());


        template.sendBody("direct:start", json);

        custom.assertIsSatisfied();
    }


    private String activityAsJson() throws TestsException, IOException {
        UUID userId = UUID.randomUUID();
        Activity activity = anActivity();
        ResolvedActivity resolvedActivity = new ResolvedActivity(userId, activity);

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(resolvedActivity);
    }


    private Activity anActivity() {
            try {
                TestsBuilder testsBuilder = TestsBuilder.getInstance();
                testsBuilder.register(new VerbRandomizer("verb-randomizer"));
                return testsBuilder.build().build(Activity.class).getObject();
            } catch (TestsException e) {
                throw new RuntimeException(e);
            }
        }
}
