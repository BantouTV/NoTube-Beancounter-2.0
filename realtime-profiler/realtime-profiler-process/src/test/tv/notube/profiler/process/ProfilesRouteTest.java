package io.beancounter.filter.process;

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

import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.ResolvedActivity;
import io.beancounter.commons.model.randomisers.VerbRandomizer;
import io.beancounter.commons.tests.TestsBuilder;
import io.beancounter.commons.tests.TestsException;
import io.beancounter.filter.FilterService;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FilterRouteTest extends CamelTestSupport {
    private Injector injector;
    private FilterService filterService;

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return injector.getInstance(FilterRoute.class);
    }

    @BeforeMethod
    public void setUp() throws Exception {

        injector = Guice.createInjector(new Module() {

            @Override
            public void configure(Binder binder) {
                filterService = mock(FilterService.class);
                binder.bind(FilterService.class).toInstance(filterService);

                binder.bind(FilterRoute.class).toInstance(new FilterRoute() {
                    @Override
                    public String fromKestrel() {
                        return "direct:start";
                    }

                    @Override
                    public String errorEndpoint() {
                        return "mock:error";
                    }

                    @Override
                    public String fromRedis() {
                        return "direct:redis";
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
    public void activityReachesFilterServices() throws Exception {
        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(0);

        String json = activityAsJson();

        template.sendBody("direct:start", json);

        error.assertIsSatisfied();
        verify(filterService).processActivity(any(ResolvedActivity.class));
    }

    @Test
    public void logsErrorFromOneOfTheServices() throws Exception {
        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(1);

        doThrow(new RuntimeException("problem")).when(filterService)
                .processActivity(any(ResolvedActivity.class));
        template.sendBody("direct:start", activityAsJson());

        error.assertIsSatisfied();
        verify(filterService).processActivity(any(ResolvedActivity.class));
    }

    @Test
    public void activityReachesCustomEndpoint() throws Exception {
        MockEndpoint custom = getMockEndpoint("mock:custom");
        custom.expectedMessageCount(1);

        String json = activityAsJson();
        when(filterService.processActivity(any(ResolvedActivity.class))).thenReturn(
                Collections.<String>emptySet());


        template.sendBody("direct:start", json);

        custom.assertIsSatisfied();
    }


    @Test
    public void redisNotificationReachesFilterService() throws Exception {
        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(0);

        String filterId = "1234";
        template.sendBody("direct:redis", filterId);

        error.assertIsSatisfied();
        verify(filterService).refresh(filterId);
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

