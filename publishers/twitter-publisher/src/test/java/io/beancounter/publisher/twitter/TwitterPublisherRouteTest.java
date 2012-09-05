package io.beancounter.publisher.twitter;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.beancounter.commons.model.activity.*;
import io.beancounter.commons.model.activity.rai.Comment;
import io.beancounter.publisher.twitter.adapters.Publisher;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.testng.CamelTestSupport;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import io.beancounter.commons.model.User;
import io.beancounter.commons.model.auth.OAuthAuth;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

import static org.mockito.Mockito.*;

public class TwitterPublisherRouteTest extends CamelTestSupport {

    private Injector injector;
    private TwitterPublisher publisher;
    private Twitter twitter;
    private ObjectMapper mapper;

    @BeforeMethod
    public void setUp() throws Exception {
        twitter = mock(Twitter.class);
        publisher = spy(new TwitterPublisher());
        injector = Guice.createInjector(new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(Twitter.class).toInstance(twitter);
                binder.bind(TwitterPublisher.class).toInstance(publisher);
                binder.bind(TwitterPublisherRoute.class).toInstance(new TwitterPublisherRoute() {
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

        mapper = new ObjectMapper();

        super.setUp();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return injector.getInstance(TwitterPublisherRoute.class);
    }

    @Test
    public void messagesWhichAreNotResolvedActivitiesAreIgnored() throws Exception {
        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(2);

        template.sendBody("direct:start", "{\"userId\":\"5609618e-7ff4-41bd-9972-0ed2bc5955f1\",\"profile\":{\"id\":\"abd4c9ba-f479-42cc-96e1-49729aa115f4\"}}");
        template.sendBody("direct:start", "{\"userId\":\"5609618f-7ff4-41bd-9972-0ed2bc5955f1\",\"data\":{\"name\":\"Bob\"}}");

        error.assertIsSatisfied();
    }

    @Test
    public void validRaiCommentIsProcessedCorrectly() throws Exception {
        String service = "twitter";
        String token = "123456abcdef";
        String username = "test-user";
        User user = new User("Bob", "Smith", username, "password");
        user.addService(service, new OAuthAuth(token, "token-secret"));

        ResolvedActivity activity = mapper.readValue(validRaiCommentActivity(), ResolvedActivity.class);
        Comment comment = (Comment) activity.getActivity().getObject();

        doReturn(mock(AccessToken.class)).when(publisher).getToken(anyString(), anyString());

        @SuppressWarnings("unchecked")
        Publisher<Comment> commentPublisher = mock(Publisher.class);

        when(commentPublisher.publish(twitter, Verb.COMMENT, comment))
                .thenReturn(mock(Status.class));

        doReturn(commentPublisher).when(publisher).getPublisher(any(Comment.class));

        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(0);

        template.sendBody("direct:start", validRaiCommentActivity());

        error.assertIsSatisfied();
        verify(commentPublisher).publish(twitter, Verb.COMMENT, comment);
    }

    @Test
    public void exceptionShouldBeHandledWhenAccessTokenIsInvalid() throws Exception {
        String service = "twitter";
        String token = "123456abcdef";
        String username = "test-user";
        User user = new User("Bob", "Smith", username, "password");
        user.addService(service, new OAuthAuth(token, "token-secret"));

        ResolvedActivity activity = mapper.readValue(validRaiCommentActivity(), ResolvedActivity.class);
        Comment comment = (Comment) activity.getActivity().getObject();

        doReturn(mock(AccessToken.class)).when(publisher).getToken(anyString(), anyString());

        @SuppressWarnings("unchecked")
        Publisher<Comment> commentPublisher = mock(Publisher.class);
        when(commentPublisher.publish(twitter, Verb.COMMENT, comment))
                .thenThrow(new TwitterPublisherException("OAuthException, Invalid OAuth access token", new TwitterException("OAuthException")));
        doReturn(commentPublisher).when(publisher).getPublisher(any(Comment.class));

        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(1);

        template.sendBody("direct:start", validRaiCommentActivity());

        error.assertIsSatisfied();
        verify(commentPublisher).publish(twitter, Verb.COMMENT, comment);
    }

    private String validRaiCommentActivity() {
        return "{\"userId\":\"5609618e-7ff4-41bd-9972-0ed2bc5955f1\",\"activity\":{\"id\":\"baea9cbd-d285-42ab-ba84-7b8316e59a75\",\"verb\":\"COMMENT\",\"object\":{\"type\":\"RAI-TV-COMMENT\",\"url\":\"http://rai.it\",\"name\":null,\"description\":null,\"text\":\"A fake comment for the twitter-filter\",\"onEvent\":\"ContentSet-07499e81-1058-4ea0-90f7-77f0fc17eade\"},\"context\":{\"date\":1340702105000,\"service\":\"http://rai.it\",\"mood\":null}},\"user\":{\"username\":\"test-user\",\"services\":{\"twitter\":{\"type\":\"OAuth\",\"session\":\"123456abcdef\",\"secret\":\"token-secret\"}}}}";
    }
}
