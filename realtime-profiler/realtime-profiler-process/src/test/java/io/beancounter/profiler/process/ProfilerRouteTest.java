package io.beancounter.profiler.process;

import java.io.IOException;
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

import io.beancounter.commons.model.User;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.ResolvedActivity;
import io.beancounter.commons.model.auth.OAuthAuth;
import io.beancounter.commons.model.randomisers.VerbRandomizer;
import io.beancounter.commons.tests.TestsBuilder;
import io.beancounter.commons.tests.TestsException;
import io.beancounter.profiler.Profiler;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ProfilerRouteTest extends CamelTestSupport {
    private Injector injector;
    private Profiler profiler;

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return injector.getInstance(ProfilerRoute.class);
    }

    @BeforeMethod
    public void setUp() throws Exception {

        injector = Guice.createInjector(new Module() {

            @Override
            public void configure(Binder binder) {
                profiler = mock(Profiler.class);
                binder.bind(Profiler.class).toInstance(profiler);

                binder.bind(ProfilerRoute.class).toInstance(new ProfilerRoute() {
                    @Override
                    public String fromKestrel() {
                        return "direct:start";
                    }

                    @Override
                    public String errorEndpoint() {
                        return "mock:error";
                    }

                    @Override
                    protected String toWriter() {
                        return "direct:writer";
                    }

                    @Override
                    protected String toAnalyser() {
                        return "direct:analyser";
                    }
                });
            }
        });
        super.setUp();
    }


    @Test
    public void activityReachesProfiler() throws Exception {
        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(0);

        template.sendBody("direct:start", activityAsJson());

        error.assertIsSatisfied();
        verify(profiler).profile(any(UUID.class), any(Activity.class));
    }

    @Test
    public void activityReachesWriter() throws Exception {
        MockEndpoint error = getMockEndpoint("mock:writer");
        error.expectedMessageCount(0);

        template.sendBody("direct:start", activityAsJson());

        error.assertIsSatisfied();
        verify(profiler).profile(any(UUID.class), any(Activity.class));
    }


    private String activityAsJson() throws TestsException, IOException {
        UUID userId = UUID.randomUUID();
        Activity activity = anActivity();
        User user = new User();
        user.setUsername("test-username");
        user.setName("test-name");
        user.setPassword("test-pwd");
        user.setSurname("test-surname");
        user.addService("test-service", new OAuthAuth("s", "c"));

        ResolvedActivity resolvedActivity = new ResolvedActivity(userId, activity, user);

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

