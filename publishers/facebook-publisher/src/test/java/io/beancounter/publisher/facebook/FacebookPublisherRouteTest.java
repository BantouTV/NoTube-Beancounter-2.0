package io.beancounter.publisher.facebook;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.types.FacebookType;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.testng.CamelTestSupport;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import io.beancounter.commons.model.User;
import io.beancounter.commons.model.auth.OAuthAuth;
import io.beancounter.resolver.Resolver;
import io.beancounter.usermanager.UserManager;

import static org.mockito.Mockito.*;

public class FacebookPublisherRouteTest extends CamelTestSupport {

    private Injector injector;
    private FacebookPublisher publisher;

    @BeforeMethod
    public void setUp() throws Exception {
        injector = Guice.createInjector(new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(FacebookPublisherRoute.class).toInstance(new FacebookPublisherRoute() {
                    @Override
                    protected String fromEndpoint() {
                        return "direct:start";
                    }

                    @Override
                    public String errorEndpoint() {
                        return "mock:error";
                    }

                    @Override
                    protected FacebookPublisher facebookPublisher() {
                        return publisher;
                    }
                });
            }
        });

        publisher = spy(injector.getInstance(FacebookPublisher.class));

        super.setUp();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return injector.getInstance(FacebookPublisherRoute.class);
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
    public void validResolvedActivityIsProcessedCorrectly() throws Exception {
        String service = "facebook";
        String token = "123456abcdef";
        String username = "test-user";
        User user = new User("Bob", "Smith", username, "password");
        user.addService(service, new OAuthAuth(token, "password"));

        doReturn(new FacebookType()).when(publisher).publishActivity(anyString(), anyString());

        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(0);

        template.sendBody("direct:start", validResolvedActivity());

        error.assertIsSatisfied();
        verify(publisher).publishActivity(token, "http://www.facebook.com/9876543211");
    }

    @Test
    public void exceptionShouldBeHandledWhenAccessTokenIsInvalid() throws Exception {
        String service = "facebook";
        String token = "123456abcdef";
        String username = "test-user";
        User user = new User("Bob", "Smith", username, "password");
        user.addService(service, new OAuthAuth(token, "password"));

        doThrow(new FacebookOAuthException("OAuthException", "Invalid OAuth access token."))
                .when(publisher).publishActivity(anyString(), anyString());

        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(1);

        template.sendBody("direct:start", validResolvedActivity());

        error.assertIsSatisfied();
        verify(publisher).publishActivity(token, "http://www.facebook.com/9876543211");
    }

    private String validResolvedActivity() {
        return "{\"userId\":\"5609618e-7ff4-41bd-9972-0ed2bc5955f1\",\"activity\":{\"id\":\"baea9cbd-d285-42ab-ba84-7b8316e59a74\",\"verb\":\"LIKE\",\"object\":{\"type\":\"FB-LIKE\",\"url\":\"http://www.facebook.com/9876543211\",\"name\":\"Prosciutto di Parma\",\"description\":null,\"categories\":[\"Food\",\"beverages\"]},\"context\":{\"date\":1343117754000,\"service\":\"facebook\",\"mood\":null,\"username\":\"10000101110123\"}},\"user\":{\"username\":\"test-user\",\"services\":{\"facebook\":{\"type\":\"OAuth\",\"session\":\"123456abcdef\"}}}}";
    }
}
