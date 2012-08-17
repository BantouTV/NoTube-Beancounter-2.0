package io.beancounter.commons.camel.tests;

import org.apache.camel.builder.RouteBuilder;

public class KestrelRoute extends RouteBuilder {
    private String host;
    private String message;

    public void configure() {

        from("timer://foo?repeatCount=1")

                .setBody(constant(message))

                .to("log:TestHelper?level=DEBUG&showAll=true&multiline=true")

                .to("kestrel://" + host);
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
