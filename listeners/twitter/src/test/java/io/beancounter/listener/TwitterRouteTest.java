package io.beancounter.listener;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import com.google.inject.*;
import io.beancounter.resolver.Resolver;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.testng.CamelTestSupport;
import org.guiceyfruit.jndi.JndiBind;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TwitterRouteTest extends CamelTestSupport {

    private Injector injector;

    private Resolver resolver;

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return injector.getInstance(TwitterRoute.class);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        injector = Guice.createInjector(new Module() {
            @Provides
            @JndiBind("serializer")
            RedisSerializer redisSerializer() {
                return new RedisSerializer<String>() {
                    private static final String CHARSET = "UTF-8";

                    @Override
                    public byte[] serialize(String s) throws SerializationException {
                        try {
                            return s.getBytes(CHARSET);
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public String deserialize(byte[] bytes) throws SerializationException {
                        try {
                            return new String(bytes, CHARSET);
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
            }
            @Override
            public void configure(Binder binder) {
                resolver = mock(Resolver.class);
                binder.bind(Resolver.class).toInstance(resolver);
                binder.bind(TwitterRoute.class).toInstance(new TwitterRoute() {
                    @Override
                    public String fromEndpoint() {
                        return "direct:start";
                    }

                    @Override
                    public String fromRegisterChannel() {
                        return "direct:redis";
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
