package tv.notube.listener;

import java.util.Date;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.testng.CamelTestSupport;
import org.testng.annotations.Test;

import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TwitterRouteTest extends CamelTestSupport {

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new TwitterRoute() {
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
        };
    }

    @Test
    public void tweetReachedDestination() throws Exception {
        MockEndpoint result = getMockEndpoint("mock:result");
        result.expectedMessageCount(1);

        Status status = aTweet();

        template.sendBody("direct:start", status);
        result.assertIsSatisfied();
    }

    @Test
    public void failedTweetsAreLogged() throws Exception {
        MockEndpoint result = getMockEndpoint("mock:result");
        result.whenAnyExchangeReceived(new Processor() {
            public void process(Exchange exchange) throws Exception {
                throw new RuntimeException("Simulated connection error");
            }
        });

        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(1);


        Status status = aTweet();
        template.sendBody("direct:start", status);
        error.assertIsSatisfied();
    }

    private Status aTweet() {
        Status status = mock(Status.class);
        User user = mock(User.class);
        when(user.getScreenName()).thenReturn("Joe");
        when(status.getUser()).thenReturn(user);
        when(status.getCreatedAt()).thenReturn(new Date());
        when(status.getURLEntities()).thenReturn(new URLEntity[0]);
        when(status.getHashtagEntities()).thenReturn(new HashtagEntity[0]);
        return status;
    }
}
