package io.beancounter.analyser.process;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.beancounter.analyser.Analyser;
import io.beancounter.analyser.AnalyserException;
import io.beancounter.commons.model.User;
import io.beancounter.commons.model.UserProfile;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.Context;
import io.beancounter.commons.model.activity.ResolvedActivity;
import io.beancounter.commons.model.activity.Tweet;
import io.beancounter.commons.model.activity.Verb;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.testng.CamelTestSupport;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RealTimeAnalyserRouteTest extends CamelTestSupport {

    private static final String ACTIVITIES = "direct:activities-start";
    private static final String PROFILES = "direct:profiles-start";
    private static final String ERROR = "mock:error";

    private Injector injector;
    private Analyser analyser;
    private ObjectMapper mapper;

    @Override
    @BeforeMethod
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        analyser = mock(Analyser.class);
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Analyser.class).toInstance(analyser);
                bind(RealTimeAnalyserRoute.class).toInstance(new RealTimeAnalyserRoute() {
                    @Override
                    protected String activitiesEndpoint() {
                        return ACTIVITIES;
                    }

                    @Override
                    protected String profilesEndpoint() {
                        return PROFILES;
                    }

                    @Override
                    protected String errorEndpoint() {
                        return ERROR;
                    }
                });
            }
        });

        super.setUp();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return injector.getInstance(RealTimeAnalyserRoute.class);
    }

    @Test
    public void messagesWhichAreNotResolvedActivitiesFromTheActivitiesEndpointShouldBeIgnored() throws Exception {
        MockEndpoint error = getMockEndpoint(ERROR);
        error.expectedMessageCount(3);

        template.sendBody(ACTIVITIES, "hello world");
        template.sendBody(ACTIVITIES, "");
        template.sendBody(ACTIVITIES, "{\"not\": [\"an\", \"activity\"]}");

        error.assertIsSatisfied();
    }

    @Test
    public void messagesWhichAreNotUserProfilesFromTheProfilesEndpointShouldBeIgnored() throws Exception {
        MockEndpoint error = getMockEndpoint(ERROR);
        error.expectedMessageCount(3);

        template.sendBody(PROFILES, "hello world");
        template.sendBody(PROFILES, "");
        template.sendBody(PROFILES, "{\"not\": [\"a\", \"profile\"]}");

        error.assertIsSatisfied();
    }

    @Test
    public void shouldPerformAnalysisOnActivity() throws Exception {
        MockEndpoint error = getMockEndpoint(ERROR);
        error.expectedMessageCount(0);

        User user = getUser();
        Activity activity = getActivity();
        ResolvedActivity resolvedActivity = new ResolvedActivity(user.getId(), activity, user);

        template.sendBody(ACTIVITIES, mapper.writeValueAsString(resolvedActivity));

        error.assertIsSatisfied();
        verify(analyser).analyse(activity);
    }

    @Test
    public void shouldPerformAnalysisOnUserProfile() throws Exception {
        MockEndpoint error = getMockEndpoint(ERROR);
        error.expectedMessageCount(0);

        UserProfile profile = getProfile();

        template.sendBody(PROFILES, mapper.writeValueAsString(profile));

        error.assertIsSatisfied();
        verify(analyser).analyse(profile);
    }

    @Test
    public void givenErrorWhenAnalysingActivityThenExpectException() throws Exception {
        MockEndpoint error = getMockEndpoint(ERROR);
        error.expectedMessageCount(1);

        User user = getUser();
        Activity activity = getActivity();
        ResolvedActivity resolvedActivity = new ResolvedActivity(user.getId(), activity, user);

        when(analyser.analyse(activity)).thenThrow(new AnalyserException());
        template.sendBody(ACTIVITIES, mapper.writeValueAsString(resolvedActivity));

        error.assertIsSatisfied();
    }

    @Test
    public void givenErrorWhenAnalysingProfileThenExpectException() throws Exception {
        MockEndpoint error = getMockEndpoint(ERROR);
        error.expectedMessageCount(1);

        UserProfile profile = getProfile();

        when(analyser.analyse(profile)).thenThrow(new AnalyserException());
        template.sendBody(PROFILES, mapper.writeValueAsString(profile));

        error.assertIsSatisfied();
    }

    private User getUser() {
        return new User("Test", "User", "test-user", "password");
    }

    private Activity getActivity() throws MalformedURLException {
        Tweet tweet = new Tweet();
        tweet.setUrl(new URL("http://twitter.com/test-user/status/123456"));
        tweet.setName("Test User");
        tweet.setText("This is a test tweet!!!");

        Context context = new Context(DateTime.now());
        context.setService("http://twitter.com");
        context.setUsername("test-user");

        Activity activity = new Activity(Verb.TWEET, tweet, context);
        activity.setId(UUID.randomUUID());

        return activity;
    }

    private UserProfile getProfile() {
        UserProfile profile = new UserProfile(UUID.randomUUID());
        profile.setLastUpdated(DateTime.now());
        return profile;
    }
}
