package io.beancounter.jmspublisher.process;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.testng.CamelTestSupport;
import org.testng.annotations.Test;

import io.beancounter.commons.model.activity.ResolvedActivity;
import it.rainet.portal.cms.client.integration.lightstreamer.LightstreamerDTO;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class JmsPublisherRouteTest extends CamelTestSupport {
    private ActivityToJmsConverter activityToJmsConverter;
    private LightstreamerDTO convertedValue;
    private JmsPublisher jmsPublisher;

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        jmsPublisher = mock(JmsPublisher.class);

        activityToJmsConverter = new ActivityToJmsConverter() {
            @Override
            public LightstreamerDTO wrapInExternalObject(ResolvedActivity resolvedActivity, String json) {
                return convertedValue;
            }
        };

        return new JmsPublisherRoute(activityToJmsConverter, jmsPublisher) {
            @Override
            protected String fromEndpoint() {
                return "direct:start";
            }


            @Override
            public String errorEndpoint() {
                return "mock:error";
            }
        };

    }

    @Test
    public void unconvertableActivitiesAreDropped() throws Exception {
        convertedValue = null;

        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(0);

        template.sendBody("direct:start", ActivityBuilder.resolvedActivityAsJson());
        error.assertIsSatisfied();
        verify(jmsPublisher, never()).publish(any(LightstreamerDTO.class));
    }

    @Test
    public void convertedActivitiesAreSentToTarget() throws Exception {
        convertedValue = new LightstreamerDTO("owner", "body", "type");

        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(0);

        template.sendBody("direct:start", ActivityBuilder.resolvedActivityAsJson());
        error.assertIsSatisfied();
        verify(jmsPublisher).publish(convertedValue);
    }
}

