package io.beancounter.listener.facebook;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.testng.CamelTestSupport;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.randomisers.VerbRandomizer;
import io.beancounter.commons.tests.TestsBuilder;
import io.beancounter.commons.tests.TestsException;
import io.beancounter.listener.commons.ActivityConverter;
import io.beancounter.listener.facebook.core.model.FacebookNotification;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FacebookRouteTest extends CamelTestSupport {
    private Injector injector;
    private List<Activity> activities;
    private ActivityConverter activityConverter;

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return injector.getInstance(FacebookRoute.class);
    }

    @BeforeMethod
    public void setUp() throws Exception {

        injector = Guice.createInjector(new Module() {

            @Override
            public void configure(Binder binder) {
                activityConverter = mock(ActivityConverter.class);
                binder.bind(ActivityConverter.class).toInstance(activityConverter);
                binder.bind(FacebookRoute.class).toInstance(new FacebookRoute() {
                    @Override
                    protected String toKestrelQueue() {
                        return "mock:result";
                    }

                    @Override
                    protected String fromFacebookEndpoint() {
                        return "direct:start";
                    }


                    @Override
                    public String errorEndpoint() {
                        return "mock:error";
                    }
                });
            }
        });


        activities = new ArrayList<Activity>();
        activities.add(anActivity());
        activities.add(anActivity());
        super.setUp();
    }

    @Test
    public void challengeParamReturnedOnSuccessfulVerification() throws Exception {
        String challenge = "success";
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("hub.mode")).thenReturn("subscribe");
        when(request.getParameter("hub.verify_token")).thenReturn("TEST-BEANCOUNTER-FACEBOOK");
        when(request.getParameter("hub.challenge")).thenReturn(challenge);

        Exchange exchange = template.request("direct:start", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                exchange.getIn().setHeader(Exchange.HTTP_SERVLET_REQUEST, request);
            }
        });

        assertThat(challenge, is(equalTo(exchange.getOut().getBody())));
    }

    @Test
    public void challengeNotReturnedWhenHubParamsAreMissing() throws Exception {
        final HttpServletRequest request = mock(HttpServletRequest.class);

        Exchange exchange = template.request("direct:start", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");
                exchange.getIn().setHeader(Exchange.HTTP_SERVLET_REQUEST, request);
            }
        });

        assertThat(exchange.getOut().getBody(), is(nullValue()));
    }


    @Test
    public void processFacebookNotification() throws Exception {
        when(activityConverter.getActivities(any(FacebookNotification.class))).thenReturn(activities);


        MockEndpoint result = getMockEndpoint("mock:result");
        result.expectedMessageCount(2);

        template.send("direct:start", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, "POST");
                exchange.getIn().setBody(facebookRequest());
            }
        });

        result.assertIsSatisfied();
    }


    @Test
    public void handlesNotificationsWithError() throws Exception {
        when(activityConverter.getActivities(any(FacebookNotification.class)))
                .thenThrow(new RuntimeException("Wrong format"));

        MockEndpoint result = getMockEndpoint("mock:result");
        result.expectedMessageCount(0);

        MockEndpoint error = getMockEndpoint("mock:error");
        error.expectedMessageCount(1);

        template.send("direct:start", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, "POST");
                exchange.getIn().setBody(facebookRequest());
            }
        });

        result.assertIsSatisfied();
        error.assertIsSatisfied();
    }

    private String facebookRequest() {
        return "{\"object\":\"user\",\"entry\":[{\"uid\":1335845740,\"changed_fields\":[\"name\",\"picture\"],\"time\":232323},{\"uid\":1234,\"changed_fields\":[\"friends\"],\"time\":232325}]}";
    }

    private Activity anActivity() {
        try {
            TestsBuilder testsBuilder = TestsBuilder.getInstance();
            testsBuilder.register(new VerbRandomizer("verb-randomizer"));
            return testsBuilder.build().build(Activity.class).getObject();
        } catch (TestsException e) {
            throw new RuntimeException(e);
        }
    }
}