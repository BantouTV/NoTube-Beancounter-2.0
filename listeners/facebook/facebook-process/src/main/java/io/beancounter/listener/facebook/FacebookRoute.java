package io.beancounter.listener.facebook;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import io.beancounter.listener.commons.ActivityConverter;
import io.beancounter.listener.facebook.core.model.FacebookNotification;

public class FacebookRoute extends RouteBuilder {

    @Inject
    private ActivityConverter activityConverter;

    @Override
    public void configure() throws Exception {
        errorHandler(deadLetterChannel(errorEndpoint()));

        from(fromFacebookEndpoint())
                .choice()
                .when(header(Exchange.HTTP_METHOD).isEqualTo("GET"))
                .to("direct:verification")
                .when(header(Exchange.HTTP_METHOD).isEqualTo("POST"))
                .to("direct:streaming");

        from("direct:verification")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        log.debug("started verification");
                        HttpServletRequest request = exchange.getIn()
                                .getHeader(Exchange.HTTP_SERVLET_REQUEST, HttpServletRequest.class);
                        if ("subscribe".equals(request.getParameter("hub.mode"))
                                && "TEST-BEANCOUNTER-FACEBOOK"
                                .equals(request.getParameter("hub.verify_token"))) {
                            exchange.getOut().setBody(request.getParameter("hub.challenge"));
                        }
                        log.debug("hub.mode [" + request.getParameter("hub.mode") + "] - hub.verify_token ["
                                + request.getParameter("hub.verify_token") + "]");
                    }
                });

        from("direct:streaming")
                .unmarshal().json(JsonLibrary.Jackson, FacebookNotification.class)
                .bean(activityConverter)
                .split(body())
                .marshal().json(JsonLibrary.Jackson)
                .log(body().toString())
                .to(toKestrelQueue());


        //for monitoring the process
        from("jetty:http://0.0.0.0:34591/facebook/ping")
                .transform(constant("PONG\n"));

    }

    protected String fromFacebookEndpoint() {
        return "jetty:http://0.0.0.0:34567/facebook";
    }

    protected String toKestrelQueue() {
        return "kestrel://{{kestrel.queue.social.url}}";
    }

    protected String errorEndpoint() {
        return "log:" + getClass().getSimpleName() + "?{{camel.log.options.error}}";
    }
}
