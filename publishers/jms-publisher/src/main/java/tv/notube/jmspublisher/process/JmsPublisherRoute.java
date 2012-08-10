package tv.notube.jmspublisher.process;

import com.google.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import it.rainet.portal.cms.client.integration.lightstreamer.LightstreamerDTO;
import tv.notube.commons.model.activity.ResolvedActivity;

public class JmsPublisherRoute extends RouteBuilder {
    private final String ORIGINAL_BODY_HEADER = "OriginalBody";

    @Inject
    private ActivityToJmsConverter activityToJmsConverter;

    public void configure() {
         errorHandler(deadLetterChannel(errorEndpoint()));

         from(fromEndpoint())

                 .setHeader(ORIGINAL_BODY_HEADER, body())

                 .unmarshal().json(JsonLibrary.Jackson, ResolvedActivity.class)

                 .process(new Processor() {
                     @Override
                     public void process(Exchange exchange) throws Exception {
                         ResolvedActivity body = exchange.getIn().getBody(ResolvedActivity.class);
                         String originalBody = exchange.getIn().getHeader(ORIGINAL_BODY_HEADER, String.class);
                         LightstreamerDTO lightstreamerDTO = activityToJmsConverter
                                 .wrapInExternalObject(body, originalBody);
                         exchange.getOut().setBody(lightstreamerDTO);
                     }
                 })

                 .filter(body().isNotNull())

                 .to(toEndpoint());
     }

    protected String toEndpoint() {
        return "jms:topic:{{topic}}?testConnectionOnStartup=true";
    }

    protected String fromEndpoint() {
        return "kestrel://{{kestrel.queue.jms.url}}";
    }

    protected String errorEndpoint() {
        return "log:" + getClass().getSimpleName() + "?{{camel.log.options.error}}";
    }
}