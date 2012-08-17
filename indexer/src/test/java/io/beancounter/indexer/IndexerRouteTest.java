package io.beancounter.indexer;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.testng.CamelTestSupport;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.beancounter.activities.ActivityStore;
import io.beancounter.commons.model.User;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.ResolvedActivity;
import io.beancounter.commons.model.auth.OAuthAuth;
import io.beancounter.commons.model.randomisers.VerbRandomizer;
import io.beancounter.commons.tests.TestsBuilder;
import io.beancounter.commons.tests.TestsException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Reference test case for {@link IndexerRoute}.
 *
 */
public class IndexerRouteTest extends CamelTestSupport {

    private Injector injector;

    private ActivityStore activityStore;

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
                binder.bind(ActivityStore.class).toInstance(activityStore);
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
                    public String statisticsEndpoint() {
                        return "log:statistics";
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

        String json = resolvedActivityAsJson();

        template.sendBody("direct:start", json);

        error.assertIsSatisfied();
        verify(activityStore).store(any(UUID.class), any(ResolvedActivity.class));
    }

    @Test
    public void logsErrorFromOneOfTheServices() throws Exception {
        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(1);

        doThrow(new RuntimeException("problem")).when(activityStore)
                .store(any(UUID.class), any(ResolvedActivity.class));
        template.sendBody("direct:start", resolvedActivityAsJson());

        error.assertIsSatisfied();
        verify(activityStore).store(any(UUID.class), any(ResolvedActivity.class));
        List<Exchange> exchanges = error.getReceivedExchanges();
    }


    private String resolvedActivityAsJson() throws TestsException, IOException {
        UUID userId = UUID.randomUUID();
        Activity activity = anActivity();
        User user = new User();
        user.setName("test-name");
        user.setPassword("abracadabra");
        user.setSurname("test-surname");
        user.setUsername("test-username");
        user.addService("facebook", new OAuthAuth("test-session", "test-secret"));
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
