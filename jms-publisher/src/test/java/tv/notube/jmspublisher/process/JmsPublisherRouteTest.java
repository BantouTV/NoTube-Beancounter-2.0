package tv.notube.jmspublisher.process;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.testng.CamelTestSupport;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import it.rainet.portal.cms.client.integration.lightstreamer.LightstreamerDTO;
import tv.notube.commons.model.activity.ResolvedActivity;

public class JmsPublisherRouteTest extends CamelTestSupport {

    private Injector injector;

    private ActivityToJmsConverter activityToJmsConverter;
    private LightstreamerDTO convertedValue = null;

    @BeforeMethod
    public void setUp() throws Exception {
        injector = Guice.createInjector(new Module() {
            @Override
            public void configure(Binder binder) {
                activityToJmsConverter = new ActivityToJmsConverter() {
                    @Override
                    public LightstreamerDTO wrapInExternalObject(ResolvedActivity resolvedActivity, String json) {
                        return convertedValue;
                    }
                };

                binder.bind(ActivityToJmsConverter.class).toInstance(activityToJmsConverter);
                binder.bind(JmsPublisherRoute.class).toInstance(new JmsPublisherRoute() {
                    @Override
                    protected String fromEndpoint() {
                        return "direct:start";
                    }

                    @Override
                    public String toEndpoint() {
                        return "mock:result";
                    }

                    @Override
                    public String errorEndpoint() {
                        return "mock:error";
                    }
                });
            }
        });
        super.setUp();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return injector.getInstance(JmsPublisherRoute.class);
    }

    @Test
    public void unconvertableActivitiesAreDropped() throws Exception {
        convertedValue = null;
        MockEndpoint result = getMockEndpoint("mock:result");
        result.expectedMessageCount(0);

        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(0);

        template.sendBody("direct:start", ActivityBuilder.resolvedActivityAsJson());
        result.assertIsSatisfied();
        error.assertIsSatisfied();
    }

    @Test
    public void convertedActivitiesAreSentToTarget() throws Exception {
        convertedValue = new LightstreamerDTO("owner", "body", "type");
        MockEndpoint result = getMockEndpoint("mock:result");
        result.expectedMessageCount(1);

        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(0);

        template.sendBody("direct:start", ActivityBuilder.resolvedActivityAsJson());
        result.assertIsSatisfied();
        error.assertIsSatisfied();
    }
}

