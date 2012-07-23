package tv.notube.dispatcher.process;

import java.util.Properties;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.testng.CamelTestSupport;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DispatcherRouteTest extends CamelTestSupport {
    private Injector injector;

    @BeforeMethod
    public void setUp() throws Exception {
        injector = Guice.createInjector(new Module() {
            @Override
            public void configure(Binder binder) {

                Properties properties = new Properties();
                properties.put("kestrel.queue.dispatcher.prefix", "mock:");
                properties.put("kestrel.queue.dispatcher.queues", "queueOne,queueTwo");
                Names.bindProperties(binder, properties);
                binder.bind(DispatcherRoute.class).toInstance(new DispatcherRoute() {
                    @Override
                    protected String fromEndpoint() {
                        return "direct:start";
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
        return injector.getInstance(DispatcherRoute.class);
    }

    @Test
    public void unresolvedActivitiesAreDropped() throws Exception {
        MockEndpoint queueOne = getMockEndpoint("mock:queueTwo");
        queueOne.expectedMessageCount(2);

        MockEndpoint queueTwo = getMockEndpoint("mock:queueTwo");
        queueTwo.expectedMessageCount(2);

        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(0);

        template.sendBody("direct:start", anActivity());
        template.sendBody("direct:start", anActivity());
        queueOne.assertIsSatisfied();
        queueTwo.assertIsSatisfied();
        error.assertIsSatisfied();
    }

    private String anActivity() {
        return "{\"id\":\"891aed77-7f8b-4a28-991f-34683a281ead\",\"verb\":\"TWEET\",\"object\":{\"type\":\"TWEET\",\"url\":\"http://twitter.com/ElliottWilson/status/220164023340118017\",\"name\":\"ElliottWilson\",\"description\":null,\"text\":\"RT @RapRadar3: RAPRADAR: New Mixtape: Theophilus London Rose Island Vol. 1 http://t.co/BynRjPJm\",\"hashTags\":[],\"urls\":[\"http://bit.ly/P5Tzc1\"]},\"context\":{\"date\":1341326168000,\"service\":\"http://sally.beancounter.io\",\"mood\":null}}";
    }
}

