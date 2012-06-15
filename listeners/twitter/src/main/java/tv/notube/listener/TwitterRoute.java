package tv.notube.listener;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

public class TwitterRoute extends RouteBuilder {

    public void configure() {

        from("twitter://streaming/filter?type=event&keywords=test&consumerKey={{consumer.key}}&consumerSecret={{consumer.secret}}&accessToken={{access.token}}&accessTokenSecret={{access.token.secret}}")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        System.out.println("sending " + exchange.getIn().getBody());
                    }
                })
                .to("kestrel://{{kestrel.queue.url}}");

        from("kestrel://{{kestrel.queue.url}}?concurrentConsumers=10&waitTimeMs=500")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        System.out.println("received " + exchange.getIn().getBody());
                    }
                });

    }
}

