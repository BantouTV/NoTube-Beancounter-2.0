package io.beancounter.platform.activities;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import io.beancounter.activities.ActivityStore;
import io.beancounter.activities.ActivityStoreException;
import io.beancounter.activities.InvalidOrderException;
import io.beancounter.activities.WildcardSearchException;
import io.beancounter.applications.ApplicationsManager;
import io.beancounter.applications.MockApplicationsManager;
import io.beancounter.commons.model.User;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.Context;
import io.beancounter.platform.ActivitiesService;
import io.beancounter.platform.ApplicationService;
import io.beancounter.platform.JacksonMixInProvider;
import io.beancounter.platform.responses.UUIDPlatformResponse;
import io.beancounter.queues.Queues;
import io.beancounter.usermanager.UserManager;
import io.beancounter.usermanager.UserManagerException;
import io.beancounter.usermanager.UserTokenManager;
import junit.framework.Assert;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.beancounter.commons.model.activity.ResolvedActivity;
import io.beancounter.commons.model.activity.Tweet;
import io.beancounter.commons.model.activity.Verb;
import io.beancounter.commons.model.activity.rai.TVEvent;
import io.beancounter.platform.APIResponse;
import io.beancounter.platform.AbstractJerseyTestCase;
import io.beancounter.platform.responses.ResolvedActivitiesPlatformResponse;
import io.beancounter.platform.responses.ResolvedActivityPlatformResponse;
import io.beancounter.platform.responses.StringPlatformResponse;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Reference test case for {@link io.beancounter.platform.ActivitiesService}
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class ActivitiesServiceTestCase extends AbstractJerseyTestCase {

    private static String APIKEY;

    private static ActivityStore activityStore;
    private static Queues queues;
    private static UserManager userManager;
    private static UserTokenManager tokenManager;

    protected ActivitiesServiceTestCase() {
        super(9995);
    }

    @Override
    protected void startFrontendService() throws IOException {
        server = new GrizzlyWebServer(9995);
        ServletAdapter ga = new ServletAdapter();
        ga.addServletListener(ActivitiesServiceTestConfig.class.getName());
        ga.setServletPath("/");
        ga.addFilter(new GuiceFilter(), "filter", null);
        server.addGrizzlyAdapter(ga, null);
        server.start();
    }

    @BeforeTest
    public void registerApp() throws Exception {
        APIKEY = registerTestApplication().toString();
    }

    @AfterTest
    public void deregisterTestApplication() throws IOException {
        HttpClient client = new HttpClient();
        String baseQuery = "application/" + APIKEY;
        DeleteMethod deleteMethod = new DeleteMethod(base_uri + baseQuery);
        client.executeMethod(deleteMethod);
    }

    @BeforeMethod
    private void resetMocks() throws Exception {
        reset(activityStore, queues, userManager, tokenManager);
    }

    @Test
    public void addActivityWithValidUserTokenShouldBeSuccessful() throws Exception {
        String baseQuery = "activities/add/%s?token=%s";
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = user.getUserToken();
        String activity = "{\"object\":" +
                "{\"type\":\"TWEET\"," +
                "\"text\":\"Just a fake tweet!\"," +
                "\"hashTags\":[\"testingBeancounter\"]," +
                "\"urls\":[\"http://fakeUrlToTest.io\"]," +
                "\"name\":\"tweet_name\"," +
                "\"description\":null," +
                "\"url\":\"http://twitter.com\"}," +
                "\"context\":" +
                "{\"date\":null," +
                "\"service\":null," +
                "\"mood\":null}," +
                "\"verb\":\"TWEET\"}";
        String query = String.format(
                baseQuery,
                username,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(tokenManager.checkTokenExists(userToken)).thenReturn(true);

        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("activity", activity);

        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        UUIDPlatformResponse response = fromJson(responseBody, UUIDPlatformResponse.class);
        assertEquals(response.getStatus(), UUIDPlatformResponse.Status.OK);
        assertEquals(response.getMessage(), "activity successfully registered");
        assertNotNull(response.getObject());

        verify(tokenManager).checkTokenExists(userToken);
        verify(queues).push(anyString());
    }

    @Test
    public void addActivityWithExpiredUserTokenShouldRespondWithError() throws Exception {
        String baseQuery = "activities/add/%s?token=%s";
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = user.getUserToken();
        String activity = "{}";
        String query = String.format(
                baseQuery,
                username,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(tokenManager.checkTokenExists(userToken)).thenReturn(false);

        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("activity", activity);

        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "User token [" + userToken + "] is not valid");

        verify(tokenManager).checkTokenExists(userToken);
    }

    @Test
    public void addActivityWithExistingWrongUserTokenShouldRespondWithError() throws Exception {
        String baseQuery = "activities/add/%s?token=%s";
        String username = "test-user";
        UUID userToken = UUID.randomUUID();
        String activity = "{}";
        String query = String.format(
                baseQuery,
                username,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(getUser(username));
        when(tokenManager.checkTokenExists(userToken)).thenReturn(true);

        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("activity", activity);

        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "User token [" + userToken + "] is not valid");
    }

    @Test
    public void addActivityWithMissingUserTokenShouldRespondWithError() throws Exception {
        String baseQuery = "activities/add/%s";
        String username = "test-user";
        String activity = "{}";
        String query = String.format(
                baseQuery,
                username
        );

        when(userManager.getUser(username)).thenReturn(getUser(username));

        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("activity", activity);

        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "Error validating user token [null]");
    }

    @Test
    public void addActivityWithValidUserTokenForUserWithNoUserTokenShouldRespondWithError() throws Exception {
        String baseQuery = "activities/add/%s?token=%s";
        String username = "test-user";
        String activity = "{}";
        UUID userToken = UUID.randomUUID();
        String query = String.format(
                baseQuery,
                username,
                userToken
        );

        User user = getUser(username);
        user.setUserToken(null);
        when(userManager.getUser(username)).thenReturn(user);

        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("activity", activity);

        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "User token [" + userToken + "] is not valid");
    }

    @Test
    public void addActivityForNonExistentUserShouldRespondWithError() throws Exception {
        String baseQuery = "activities/add/%s?token=%s";
        String username = "non-existent-user";
        String activity = "{}";
        UUID userToken = UUID.randomUUID();
        String query = String.format(
                baseQuery,
                username,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(null);

        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("activity", activity);

        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "user with username [" + username + "] not found");
    }

    @Test
    public void testAddActivityWithANullDate() throws Exception {
        String baseQuery = "activities/add/%s?token=%s";
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = user.getUserToken();
        String activity = "{\"object\":" +
                "{\"type\":\"TWEET\"," +
                "\"text\":\"Just a fake tweet!\"," +
                "\"hashTags\":[\"testingBeancounter\"]," +
                "\"urls\":[\"http://fakeUrlToTest.io\"]," +
                "\"name\":\"tweet_name\"," +
                "\"description\":null," +
                "\"url\":\"http://twitter.com\"}," +
                "\"context\":" +
                "{\"date\":null," +
                "\"service\":null," +
                "\"mood\":null}," +
                "\"verb\":\"TWEET\"}";
        String query = String.format(
                baseQuery,
                username,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(tokenManager.checkTokenExists(userToken)).thenReturn(true);

        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("activity", activity);

        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        APIResponse actual = fromJson(responseBody, APIResponse.class);
        assertEquals(actual.getMessage(), "activity successfully registered");
        assertEquals(actual.getStatus(), "OK");
        assertNotNull(actual.getObject());
        UUID returnedActivityId = UUID.fromString(actual.getObject());
        assertNotNull(returnedActivityId);

        ArgumentCaptor captor = ArgumentCaptor.forClass(String.class);
        verify(queues).push((String) captor.capture());
        String resolvedActivity = (String) captor.getValue();
        ObjectMapper mapper = new ObjectMapper();
        ResolvedActivity expected = mapper.readValue(resolvedActivity, ResolvedActivity.class);
        Assert.assertNotNull(expected.getActivity().getContext().getDate());
    }

    @Test
    public void addActivityWithInvalidUserTokenShouldRespondWithError() throws Exception {
        String baseQuery = "activities/add/%s?token=%s";
        String username = "test-user";
        String activity = "{}";
        String userToken = "123456abcdef";
        String query = String.format(
                baseQuery,
                username,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(getUser(username));

        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("activity", activity);

        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        APIResponse actual = fromJson(responseBody, APIResponse.class);
        assertEquals(actual.getMessage(), "Error validating user token [" + userToken + "]");
        assertEquals(actual.getStatus(), "NOK");
    }

    @Test
    public void getSingleActivity() throws Exception {
        UUID activityId = UUID.randomUUID();
        Activity activity = new Activity();
        activity.setId(activityId);
        ResolvedActivity resolvedActivity = new ResolvedActivity(null, activity, null);

        String baseQuery = "activities/%s?apikey=%s";
        String query = String.format(
                baseQuery,
                activityId.toString(),
                APIKEY
        );

        when(activityStore.getActivity(activityId)).thenReturn(resolvedActivity);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        ResolvedActivityPlatformResponse response = fromJson(responseBody, ResolvedActivityPlatformResponse.class);

        assertEquals(response.getMessage(), "activity with id [" + activityId + "] found");
        assertEquals(response.getStatus().toString(), "OK");

        ResolvedActivity responseActivity = response.getObject();
        assertNotNull(responseActivity);
        assertEquals(responseActivity.getActivity().getId(), activityId);
    }

    @Test
    public void getNonExistentSingleActivity() throws Exception {
        UUID activityId = UUID.randomUUID();
        String baseQuery = "activities/%s?apikey=%s";
        String query = String.format(
                baseQuery,
                activityId.toString(),
                APIKEY
        );

        when(activityStore.getActivity(activityId)).thenReturn(null);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        ResolvedActivityPlatformResponse actual = fromJson(responseBody, ResolvedActivityPlatformResponse.class);

        assertEquals(actual.getMessage(), "no activity with id [" + activityId + "]");
        assertEquals(actual.getStatus().toString(), "OK");
        assertNull(actual.getObject());
    }

    @Test
    public void getSingleActivityWithInvalidApiKeyShouldRespondWithError() throws IOException {
        UUID activityId = UUID.randomUUID();
        String baseQuery = "activities/%s?apikey=%s";
        String query = String.format(
                baseQuery,
                activityId.toString(),
                "123456789abcdef-invalid"
        );

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse actual = fromJson(responseBody, StringPlatformResponse.class);

        assertEquals(actual.getMessage(), "Your apikey is not well formed");
        assertEquals(actual.getStatus().toString(), "NOK");
    }

    @Test
    public void getSingleActivityWithWrongParametersShouldRespondWithError() throws IOException {
        UUID activityId = UUID.randomUUID();
        String baseQuery = "activities/%s";
        String query = String.format(
                baseQuery,
                activityId.toString()
        );

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse actual = fromJson(responseBody, StringPlatformResponse.class);

        assertEquals(actual.getMessage(), "Error while checking parameters");
        assertEquals(actual.getStatus().toString(), "NOK");
    }

    @Test
    public void hideAnExistingVisibleActivity() throws Exception {
        UUID activityId = UUID.randomUUID();
        String baseQuery = "activities/%s/visible/%s?apikey=%s";
        String query = String.format(
                baseQuery,
                activityId.toString(),
                "false",
                APIKEY
        );

        PutMethod getMethod = new PutMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse actual = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(actual.getMessage(), "activity [" + activityId + "] visibility has been modified to [false]");
        assertEquals(actual.getStatus().toString(), "OK");
    }

    @Test
    public void unhideAnExistingInvisibleActivity() throws Exception {
        UUID activityId = UUID.randomUUID();
        String baseQuery = "activities/%s/visible/%s?apikey=%s";
        String query = String.format(
                baseQuery,
                activityId.toString(),
                "true",
                APIKEY
        );

        PutMethod getMethod = new PutMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getMessage(), "activity [" + activityId + "] visibility has been modified to [true]");
        assertEquals(response.getStatus().toString(), "OK");
    }

    @Test
    public void hidingANonExistentActivityShouldRespondWithAnError() throws Exception {
        UUID activityId = UUID.randomUUID();
        String baseQuery = "activities/%s/visible/%s?apikey=%s";
        String expectedMessage = "Error modifying the visibility of activity with id [" + activityId + "]";
        String query = String.format(
                baseQuery,
                activityId.toString(),
                "false",
                APIKEY
        );

        doThrow(new ActivityStoreException(expectedMessage))
                .when(activityStore).setVisible(activityId, false);

        PutMethod getMethod = new PutMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getMessage(), expectedMessage);
        assertEquals(response.getStatus().toString(), "NOK");
    }

    @Test
    public void getSingleUserActivityWithValidUserToken() throws Exception {
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = user.getUserToken();

        UUID activityId = UUID.randomUUID();
        Activity activity = new Activity();
        activity.setId(activityId);
        ResolvedActivity resolvedActivity = new ResolvedActivity(user.getId(), activity, null);

        String baseQuery = "activities/%s/%s?token=%s";
        String query = String.format(
                baseQuery,
                username,
                activityId.toString(),
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(tokenManager.checkTokenExists(userToken)).thenReturn(true);
        when(activityStore.getActivity(activityId)).thenReturn(resolvedActivity);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        ResolvedActivityPlatformResponse response = fromJson(responseBody, ResolvedActivityPlatformResponse.class);
        assertEquals(response.getStatus(), ResolvedActivityPlatformResponse.Status.OK);
        assertEquals(response.getMessage(), "activity with id [" + activityId + "] found");

        ResolvedActivity responseActivity = response.getObject();
        assertNotNull(responseActivity);
        assertEquals(responseActivity.getActivity().getId(), activityId);
    }

    @Test
    public void getSingleNonExistentUserActivityWithValidUserToken() throws Exception {
        String baseQuery = "activities/%s/%s?token=%s";
        UUID activityId = UUID.randomUUID();
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = user.getUserToken();
        String query = String.format(
                baseQuery,
                username,
                activityId.toString(),
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(tokenManager.checkTokenExists(userToken)).thenReturn(true);
        when(activityStore.getActivity(activityId)).thenReturn(null);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        ResolvedActivityPlatformResponse response = fromJson(responseBody, ResolvedActivityPlatformResponse.class);
        assertEquals(response.getStatus(), ResolvedActivityPlatformResponse.Status.OK);
        assertEquals(response.getMessage(), "no activity with id [" + activityId + "]");
        assertNull(response.getObject());
    }

    @Test
    public void getSingleUserActivityWithInvalidActivityIdShouldRespondWithError() throws Exception {
        String baseQuery = "activities/%s/%s?token=%s";
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = user.getUserToken();
        String activityId = "invalid-id-123";
        String query = String.format(
                baseQuery,
                username,
                activityId,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(tokenManager.checkTokenExists(userToken)).thenReturn(true);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "Error while getting activity [" + activityId + "]");
    }

    @Test
    public void givenActivityStoreExceptionWhenGettingSingleUserActivityThenRespondWithError() throws Exception {
        String baseQuery = "activities/%s/%s?token=%s";
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = user.getUserToken();
        UUID activityId = UUID.randomUUID();
        String query = String.format(
                baseQuery,
                username,
                activityId,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(tokenManager.checkTokenExists(userToken)).thenReturn(true);
        when(activityStore.getActivity(activityId)).thenThrow(new ActivityStoreException("error"));

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "Error while getting activity [" + activityId + "]");
    }

    @Test
    public void getSingleUserActivityForNonExistentUserShouldRespondWithError() throws Exception {
        String baseQuery = "activities/%s/%s?token=%s";
        String username = "non-existent-user";
        String query = String.format(
                baseQuery,
                username,
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        when(userManager.getUser(username)).thenReturn(null);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "user with username [" + username + "] not found");
    }

    @Test
    public void getSingleUserActivityWithWrongUserTokenShouldRespondWithError() throws Exception {
        String baseQuery = "activities/%s/%s?token=%s";
        String username = "test-user";
        UUID userToken = UUID.randomUUID();
        String query = String.format(
                baseQuery,
                username,
                UUID.randomUUID(),
                userToken
        );

        when(userManager.getUser(username)).thenReturn(getUser(username));

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "User token [" + userToken + "] is not valid");
    }

    @Test
    public void getSingleUserActivityWithMalformedUserTokenShouldRespondWithError() throws Exception {
        String baseQuery = "activities/%s/%s?token=%s";
        String username = "test-user";
        String userToken = "invalid-token-123";
        String query = String.format(
                baseQuery,
                username,
                UUID.randomUUID(),
                userToken
        );

        when(userManager.getUser(username)).thenReturn(getUser(username));

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "Error validating user token [" + userToken + "]");
    }

    @Test
    public void getSingleUserActivityWithExpiredUserTokenShouldRespondWithError() throws Exception {
        String baseQuery = "activities/%s/%s?token=%s";
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = user.getUserToken();
        String query = String.format(
                baseQuery,
                username,
                UUID.randomUUID(),
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(tokenManager.checkTokenExists(userToken)).thenReturn(false);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "User token [" + userToken + "] is not valid");
    }

    @Test
    public void givenTokenManagerErrorWhenGettingSingleUserActivityThenRespondWithError() throws Exception {
        String baseQuery = "activities/%s/%s?token=%s";
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = user.getUserToken();
        String query = String.format(
                baseQuery,
                username,
                UUID.randomUUID(),
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(tokenManager.checkTokenExists(userToken)).thenThrow(new UserManagerException("error"));

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "Error validating user token [" + userToken + "]");
    }

    @Test
    public void getSingleActivityOfUserWithoutCorrectAuthShouldRespondWithError() throws Exception {
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = user.getUserToken();

        UUID activityId = UUID.randomUUID();
        Activity activity = new Activity();
        activity.setId(activityId);
        ResolvedActivity resolvedActivity = new ResolvedActivity(null, activity, getUser("other-user"));

        String baseQuery = "activities/%s/%s?token=%s";
        String query = String.format(
                baseQuery,
                username,
                activityId,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(tokenManager.checkTokenExists(userToken)).thenReturn(true);
        when(activityStore.getActivity(activityId)).thenReturn(resolvedActivity);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "User [" + username + "] is not authorized to see activity [" + activityId + "]");
    }

    @Test
    public void getAllActivitiesForNonExistentUserShouldRespondWithError() throws Exception {
        String baseQuery = "activities/all/%s?token=%s";
        String username = "non-existent-user";
        UUID userToken = UUID.randomUUID();
        String query = String.format(
                baseQuery,
                username,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(null);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "user with username [" + username + "] not found");
    }

    @Test
    public void getAllActivitiesWithWrongUserTokenShouldRespondWithError() throws Exception {
        String baseQuery = "activities/all/%s?token=%s";
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = UUID.randomUUID();
        String query = String.format(
                baseQuery,
                username,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "User token [" + userToken + "] is not valid");
    }

    @Test
    public void getAllActivitiesWithInvalidUserTokenShouldRespondWithError() throws Exception {
        String baseQuery = "activities/all/%s?token=%s";
        String username = "test-user";
        User user = getUser(username);
        String userToken = "invalid12345";
        String query = String.format(
                baseQuery,
                username,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "Error validating user token [" + userToken + "]");
    }

    @Test
    public void getAllActivitiesWithMissingUserTokenShouldRespondWithError() throws Exception {
        String baseQuery = "activities/all/%s";
        String username = "test-user";
        String query = String.format(
                baseQuery,
                username
        );

        when(userManager.getUser(username)).thenReturn(getUser(username));

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "Error validating user token [null]");
    }

    @Test
    public void getAllActivitiesWithExpiredUserTokenShouldRespondWithError() throws Exception {
        String baseQuery = "activities/all/%s?token=%s";
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = user.getUserToken();
        String query = String.format(
                baseQuery,
                username,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(tokenManager.checkTokenExists(userToken)).thenReturn(false);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "User token [" + userToken + "] is not valid");
    }

    @Test
    public void givenTokenManagerErrorOccursWhenGettingAllActivitiesThenRespondWithError() throws Exception {
        String baseQuery = "activities/all/%s?token=%s";
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = user.getUserToken();
        String query = String.format(
                baseQuery,
                username,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(tokenManager.checkTokenExists(userToken)).thenThrow(new UserManagerException("error"));

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "Error validating user token [" + userToken + "]");
    }

    @Test
    public void testGetAllActivitiesDescendingDefault() throws Exception {
        String baseQuery = "activities/all/%s?token=%s";
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = user.getUserToken();
        int page = 0;
        String order = "desc";
        String query = String.format(
                baseQuery,
                username,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(activityStore.getByUserPaginated(user.getId(), page, 20, order))
                .thenReturn(createSearchResults(page, 20, order));
        when(tokenManager.checkTokenExists(userToken)).thenReturn(true);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        ResolvedActivitiesPlatformResponse response = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        assertEquals(response.getMessage(), "user 'test-user' activities found.");
        assertEquals(response.getStatus().toString(), "OK");

        List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>(response.getObject());
        assertEquals(activities.size(), 20);
        for (int i = 0; i < activities.size(); i++) {
            Tweet tweet = (Tweet) activities.get(i).getActivity().getObject();
            assertEquals(tweet.getText(), "Fake text #" + i);
        }
    }

    @Test
    public void testGetAllActivitiesDescendingNormal() throws Exception {
        String baseQuery = "activities/all/%s?page=%s&token=%s";
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = user.getUserToken();
        int page = 1;
        String order = "desc";
        String query = String.format(
                baseQuery,
                username,
                page,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(activityStore.getByUserPaginated(user.getId(), page, 20, order))
                .thenReturn(createSearchResults(page, 20, order));
        when(tokenManager.checkTokenExists(userToken)).thenReturn(true);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        ResolvedActivitiesPlatformResponse response = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        assertEquals(response.getMessage(), "user 'test-user' activities found.");
        assertEquals(response.getStatus().toString(), "OK");

        List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>(response.getObject());
        assertEquals(activities.size(), 20);

        int i = 20;
        for (ResolvedActivity activity : activities) {
            Tweet tweet = (Tweet) activity.getActivity().getObject();
            assertEquals(tweet.getText(), "Fake text #" + i++);
        }
    }

    @Test
    public void testGetAllActivitiesDescendingMore() throws Exception {
        String baseQuery = "activities/all/%s?page=%s&order=%s&token=%s";
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = user.getUserToken();
        int page = 2;
        String order = "desc";
        String query = String.format(
                baseQuery,
                username,
                page,
                order,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(activityStore.getByUserPaginated(user.getId(), page, 20, order))
                .thenReturn(createSearchResults(page, 20, order));
        when(tokenManager.checkTokenExists(userToken)).thenReturn(true);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        ResolvedActivitiesPlatformResponse response = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        assertEquals(response.getMessage(), "user 'test-user' activities found.");
        assertEquals(response.getStatus().toString(), "OK");

        List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>(response.getObject());
        assertEquals(activities.size(), 10);

        int i = 40;
        for (ResolvedActivity activity : activities) {
            Tweet tweet = (Tweet) activity.getActivity().getObject();
            assertEquals(tweet.getText(), "Fake text #" + i++);
        }
    }

    @Test
    public void testGetAllActivitiesDescendingTooMany() throws Exception {
        String baseQuery = "activities/all/%s?page=%s&token=%s";
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = user.getUserToken();
        int page = 3;
        String order = "desc";
        String query = String.format(
                baseQuery,
                username,
                page,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(activityStore.getByUserPaginated(user.getId(), page, 20, order))
                .thenReturn(createSearchResults(page, 20, order));
        when(tokenManager.checkTokenExists(userToken)).thenReturn(true);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        ResolvedActivitiesPlatformResponse response = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        assertEquals(response.getMessage(), "user 'test-user' has no more activities.");
        assertEquals(response.getStatus().toString(), "OK");
        assertNotNull(response.getObject());
        assertEquals(response.getObject().size(), 0);
    }

    @Test
    public void getAllActivitiesForUserWithNoActivities() throws Exception {
        String baseQuery = "activities/all/%s?token=%s";
        String username = "user-with-no-activities";
        User user = getUser(username);
        UUID userToken = user.getUserToken();
        String query = String.format(
                baseQuery,
                username,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(activityStore.getByUserPaginated(user.getId(), 0, 20, "desc"))
                .thenReturn(Collections.<ResolvedActivity>emptyList());
        when(tokenManager.checkTokenExists(userToken)).thenReturn(true);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertFalse(responseBody.isEmpty());
        assertEquals(result, HttpStatus.SC_OK);

        ResolvedActivitiesPlatformResponse response = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        assertEquals(response.getMessage(), "user 'user-with-no-activities' has no activities.");
        assertEquals(response.getStatus().toString(), "OK");
        assertNotNull(response.getObject());
        assertEquals(response.getObject().size(), 0);
    }

    @Test
    public void testGetAllActivitiesAscendingDefault() throws Exception {
        String baseQuery = "activities/all/%s?token=%s&order=%s";
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = user.getUserToken();
        String order = "asc";
        String query = String.format(
                baseQuery,
                username,
                userToken,
                order
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(activityStore.getByUserPaginated(user.getId(), 0, 20, order))
                .thenReturn(createSearchResults(0, 20, order));
        when(tokenManager.checkTokenExists(userToken)).thenReturn(true);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        ResolvedActivitiesPlatformResponse response = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        assertEquals(response.getMessage(), "user '" + username + "' activities found.");
        assertEquals(response.getStatus().toString(), "OK");

        List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>(response.getObject());
        assertEquals(activities.size(), 20);
        for (int i = 0; i < activities.size(); i++) {
            Tweet tweet = (Tweet) activities.get(i).getActivity().getObject();
            assertEquals(tweet.getText(), "Fake text #" + (49 - i));
        }
    }

    @Test
    public void testGetAllActivitiesAscendingNormal() throws Exception {
        String baseQuery = "activities/all/%s?page=%s&order=%s&token=%s";
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = user.getUserToken();
        int page = 1;
        String order = "asc";
        String query = String.format(
                baseQuery,
                username,
                page,
                order,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(activityStore.getByUserPaginated(user.getId(), page, 20, order))
                .thenReturn(createSearchResults(page, 20, order));
        when(tokenManager.checkTokenExists(userToken)).thenReturn(true);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        ResolvedActivitiesPlatformResponse response = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        assertEquals(response.getMessage(), "user 'test-user' activities found.");
        assertEquals(response.getStatus().toString(), "OK");

        List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>(response.getObject());
        assertEquals(activities.size(), 20);

        int i = 20;
        for (ResolvedActivity activity : activities) {
            Tweet tweet = (Tweet) activity.getActivity().getObject();
            assertEquals(tweet.getText(), "Fake text #" + (49 - i++));
        }
    }

    @Test
    public void testGetAllActivitiesAscendingMore() throws Exception {
        String baseQuery = "activities/all/%s?page=%s&order=%s&token=%s";
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = user.getUserToken();
        int page = 2;
        String order = "asc";
        String query = String.format(
                baseQuery,
                username,
                page,
                order,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(activityStore.getByUserPaginated(user.getId(), page, 20, order))
                .thenReturn(createSearchResults(page, 20, order));
        when(tokenManager.checkTokenExists(userToken)).thenReturn(true);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        ResolvedActivitiesPlatformResponse response = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        assertEquals(response.getMessage(), "user 'test-user' activities found.");
        assertEquals(response.getStatus().toString(), "OK");

        List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>(response.getObject());
        assertNotNull(activities);
        assertEquals(activities.size(), 10);

        int i = 40;
        for (ResolvedActivity activity : activities) {
            Tweet tweet = (Tweet) activity.getActivity().getObject();
            assertEquals(tweet.getText(), "Fake text #" + (49 - i++));
        }
    }

    @Test
    public void getAllUserActivitiesWithInvalidSortOrderParameterReturnsErrorResponse() throws Exception {
        String baseQuery = "activities/all/%s?order=%s&token=%s";
        String username = "test-user";
        String order = "invalid-order";
        User user = getUser(username);
        UUID userToken = user.getUserToken();
        String query = String.format(
                baseQuery,
                username,
                order,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(activityStore.getByUserPaginated(user.getId(), 0, 20, order))
                .thenThrow(new InvalidOrderException(order + " is not a valid sort order."));
        when(tokenManager.checkTokenExists(userToken)).thenReturn(true);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        assertNotEquals(responseBody, "");

        ResolvedActivitiesPlatformResponse actual = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);

        assertEquals(actual.getMessage(), order + " is not a valid sort order.");
        assertEquals(actual.getStatus().toString(), "NOK");
        assertNull(actual.getObject());
    }

    @Test
    public void searchForCustomActivityWithToken() throws Exception {
        String baseQuery = "activities/search/me?path=%s&value=%s&order=%s&token=%s";
        String path = "type";
        String value = "RAI-CONTENT-ITEM";
        String order = "desc";
        User user = getUser("test-user");
        UUID userToken = user.getUserToken();
        String query = String.format(
                baseQuery,
                path,
                value,
                order,
                userToken
        );

        List<ResolvedActivity> results = new ArrayList<ResolvedActivity>();
        results.add(createCustomActivity());
        when(activityStore.search(path, value, 0, 20, order, Collections.<String>emptyList())).thenReturn(results);
        when(tokenManager.checkTokenExists(userToken)).thenReturn(true);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        ResolvedActivitiesPlatformResponse response = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        assertEquals(response.getMessage(), "search for [type=RAI-CONTENT-ITEM] found activities.");
        assertEquals(response.getStatus().toString(), "OK");

        List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>(response.getObject());
        assertEquals(activities.size(), 1);

        TVEvent tvEvent = (TVEvent) activities.get(0).getActivity().getObject();
        assertEquals(tvEvent.getName(), "Euro 2012");
    }

    @Test
    public void searchForCustomActivityWithNotValidToken() throws Exception {
        String baseQuery = "activities/search/me?path=%s&value=%s&order=%s&token=%s";
        String path = "type";
        String value = "RAI-CONTENT-ITEM";
        String order = "desc";
        User user = getUser("test-user");
        UUID userToken = user.getUserToken();
        String query = String.format(
                baseQuery,
                path,
                value,
                order,
                userToken
        );

        List<ResolvedActivity> results = new ArrayList<ResolvedActivity>();
        results.add(createCustomActivity());
        when(activityStore.search(path, value, 0, 20, order, Collections.<String>emptyList())).thenReturn(results);
        when(tokenManager.checkTokenExists(userToken)).thenReturn(false);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        ResolvedActivitiesPlatformResponse response = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        assertEquals(response.getMessage(), "User token [" + userToken.toString() + "] is not valid");
        assertEquals(response.getStatus().toString(), "NOK");
        assertNull(response.getObject());
    }

    @Test
    public void searchForCustomActivityWithValidApiKey() throws Exception {
        String baseQuery = "activities/search?path=%s&value=%s&order=%s&apikey=%s";
        String path = "type";
        String value = "RAI-CONTENT-ITEM";
        String order = "desc";
        String query = String.format(
                baseQuery,
                path,
                value,
                order,
                APIKEY
        );

        List<ResolvedActivity> results = new ArrayList<ResolvedActivity>();
        results.add(createCustomActivity());
        when(activityStore.search(path, value, 0, 20, order, Collections.<String>emptyList())).thenReturn(results);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        ResolvedActivitiesPlatformResponse response = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        assertEquals(response.getMessage(), "search for [type=RAI-CONTENT-ITEM] found activities.");
        assertEquals(response.getStatus().toString(), "OK");

        List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>(response.getObject());
        assertEquals(activities.size(), 1);

        TVEvent tvEvent = (TVEvent) activities.get(0).getActivity().getObject();
        assertEquals(tvEvent.getName(), "Euro 2012");
    }

    @Test
    public void searchForAllTweetsMostRecentFirst() throws Exception {
        int i = 0;
        int page = 0;
        int tweetCount = 0;

        while (true) {
            String baseQuery = "activities/search?path=%s&value=%s&page=%d&apikey=%s";
            String path = "type";
            String value = Verb.TWEET.name();
            String query = String.format(
                    baseQuery,
                    path,
                    value,
                    page,
                    APIKEY
            );

            when(activityStore.search(path, value, page, 20, "desc", Collections.<String>emptyList()))
                    .thenReturn(createSearchResults(page, 20, "desc"));

            GetMethod getMethod = new GetMethod(base_uri + query);
            HttpClient client = new HttpClient();

            int result = client.executeMethod(getMethod);
            String responseBody = new String(getMethod.getResponseBody());
            assertEquals(result, HttpStatus.SC_OK);
            assertFalse(responseBody.isEmpty());

            ResolvedActivitiesPlatformResponse response = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);

            if (response.getObject().isEmpty()) {
                assertEquals(response.getStatus().toString(), "OK");
                assertEquals(response.getMessage(), "search for [type=TWEET] found no more activities.");
                assertNotNull(response.getObject());
                assertEquals(response.getObject().size(), 0);
                break;
            }

            assertEquals(response.getMessage(), "search for [type=TWEET] found activities.");
            assertEquals(response.getStatus().toString(), "OK");

            List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>(response.getObject());
            for (ResolvedActivity activity : activities) {
                Tweet tweet = (Tweet) activity.getActivity().getObject();
                assertEquals(tweet.getText(), "Fake text #" + i++);
            }
            tweetCount += activities.size();

            page++;
        }

        assertEquals(tweetCount, 50);
    }

    @Test
    public void searchForAllTweetsEarliestFirst() throws Exception {
        int i = 0;
        int page = 0;
        int tweetCount = 0;

        while (true) {
            String baseQuery = "activities/search?path=%s&value=%s&page=%d&order=%s&apikey=%s";
            String path = "type";
            String value = Verb.TWEET.name();
            String order = "asc";
            String query = String.format(
                    baseQuery,
                    path,
                    value,
                    page,
                    order,
                    APIKEY
            );

            when(activityStore.search(path, value, page, 20, order, Collections.<String>emptyList()))
                    .thenReturn(createSearchResults(page, 20, order));

            GetMethod getMethod = new GetMethod(base_uri + query);
            HttpClient client = new HttpClient();

            int result = client.executeMethod(getMethod);
            String responseBody = new String(getMethod.getResponseBody());
            assertEquals(result, HttpStatus.SC_OK);
            assertFalse(responseBody.isEmpty());

            ResolvedActivitiesPlatformResponse response = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);

            if (response.getObject().isEmpty()) {
                assertEquals(response.getStatus().toString(), "OK");
                assertEquals(response.getMessage(), "search for [type=TWEET] found no more activities.");
                assertNotNull(response.getObject());
                assertEquals(response.getObject().size(), 0);
                break;
            }

            assertEquals(response.getMessage(), "search for [type=TWEET] found activities.");
            assertEquals(response.getStatus().toString(), "OK");

            List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>(response.getObject());
            for (ResolvedActivity activity : activities) {
                Tweet tweet = (Tweet) activity.getActivity().getObject();
                assertEquals(tweet.getText(), "Fake text #" + (49 - i++));
            }
            tweetCount += activities.size();

            page++;
        }

        assertEquals(tweetCount, 50);
    }

    @Test
    public void searchingWithInvalidSortOrderParameterReturnsErrorResponse() throws Exception {
        String baseQuery = "activities/search?path=%s&value=%s&order=%s&apikey=%s";
        String order = "invalid-order";
        String path = "type";
        String value = "TWEET";
        String query = String.format(
                baseQuery,
                path,
                value,
                order,
                APIKEY
        );

        when(activityStore.search(path, value, 0, 20, order, Collections.<String>emptyList()))
                .thenThrow(new InvalidOrderException(order + " is not a valid sort order."));

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        ResolvedActivitiesPlatformResponse response = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        assertEquals(response.getMessage(), order + " is not a valid sort order.");
        assertEquals(response.getStatus().toString(), "NOK");
        assertNull(response.getObject());
    }

    @Test
    public void searchWithWildcardsShouldFail() throws Exception {
        String baseQuery = "activities/search?path=%s&value=%s&apikey=%s";
        String expectedMessage = "Wildcard searches are not allowed.";
        String query = String.format(
                baseQuery,
                "*",
                "*",
                APIKEY
        );

        when(activityStore.search("*", "*", 0, 20, "desc", Collections.<String>emptyList()))
                .thenThrow(new WildcardSearchException(expectedMessage));

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        ResolvedActivitiesPlatformResponse response = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        assertEquals(response.getStatus().toString(), "NOK");
        assertEquals(response.getMessage(), expectedMessage);
        assertNull(response.getObject());
    }

    @Test
    public void wildcardsAreNotAllowedInSearchFilters() throws Exception {
        String baseQuery = "activities/search?path=%s&value=%s&filter=%s&apikey=%s";
        String expectedMessage = "Wildcard searches are not allowed.";
        String path = "type";
        String value = Verb.TWEET.name();
        String query = String.format(
                baseQuery,
                path,
                value,
                "url:*",
                APIKEY
        );

        List<String> filters = Arrays.asList("url:*");
        when(activityStore.search(path, value, 0, 20, "desc", filters))
                .thenThrow(new WildcardSearchException(expectedMessage));

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int responseCode = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(responseCode, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        ResolvedActivitiesPlatformResponse response = fromJson(responseBody, ResolvedActivitiesPlatformResponse.class);
        assertEquals(response.getStatus().toString(), "NOK");
        assertEquals(response.getMessage(), expectedMessage);
        assertNull(response.getObject());
    }

    @Test
    public void incorrectlyFormattedFiltersCauseErrorsToBeReturned() throws Exception {
        String baseQuery = "activities/search?path=%s&value=%s&filter=%s&apikey=%s";
        String expectedMessage = "Incorrectly formatted filter";
        String path = "type";
        String value = Verb.TWEET.name();

        for (String filter : Arrays.asList("invalid:", ":invalid", "invalid")) {
            String query = String.format(
                    baseQuery,
                    path,
                    value,
                    filter,
                    APIKEY
            );

            List<String> filters = Arrays.asList(filter);
            when(activityStore.search(path, value, 0, 20, "desc", filters))
                    .thenThrow(new ActivityStoreException(expectedMessage));

            GetMethod getMethod = new GetMethod(base_uri + query);
            HttpClient client = new HttpClient();

            int responseCode = client.executeMethod(getMethod);
            String responseBody = new String(getMethod.getResponseBody());
            assertEquals(responseCode, HttpStatus.SC_INTERNAL_SERVER_ERROR);
            assertFalse(responseBody.isEmpty());

            StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
            assertEquals(response.getStatus().toString(), "NOK");
            assertEquals(response.getMessage(), "Error while getting page " + 0 + " of activities where [" + path + "=" + value +"]");
            assertEquals(response.getObject(), expectedMessage);
        }
    }

    @Test
    public void testCustomActivityContentItem() throws Exception {
        String baseQuery = "activities/add/%s?token=%s";
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = user.getUserToken();
        String activity = "{\n" +
                "    \"verb\": \"WATCHED\",\n" +
                "    \"object\": {\n" +
                "        \"type\": \"RAI-CONTENT-ITEM\",\n" +
                "        \"url\": \"http://www.rai.tv/dl/RaiTV/programmi/media/ContentItem-17efdae2-c803-4411-aac9-f6185bdf13de.html\",\n" +
                "        \"name\": \"test-name\",\n" +
                "        \"description\": \"test-description\",\n" +
                "        \"id\": \"17efdae2-c803-4411-aac9-f6185bdf13de\"\n" +
                "    },\n" +
                "    \"context\": {\n" +
                "        \"date\": 1342456531059,\n" +
                "        \"service\": \"rai.tv\",\n" +
                "        \"mood\": null,\n" +
                "        \"username\": \"dpalmisano\"\n" +
                "    }\n" +
                "}";
        String query = String.format(
                baseQuery,
                username,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(tokenManager.checkTokenExists(userToken)).thenReturn(true);

        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("activity", activity);
        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertFalse(responseBody.isEmpty());
        assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");

        APIResponse actual = fromJson(responseBody, APIResponse.class);
        assertEquals(actual.getMessage(), "activity successfully registered");
        assertEquals(actual.getStatus(), "OK");
    }

    @Test
    public void testCustomActivityTvEvent() throws Exception {
        String baseQuery = "activities/add/%s?token=%s";
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = user.getUserToken();
        String activity = "{\n" +
                "    \"verb\": \"CHECKIN\",\n" +
                "    \"object\": {\n" +
                "        \"type\": \"RAI-TV-EVENT\",\n" +
                "        \"url\": \"http://www.rai.tv/dl/RaiTV/programmi/media/EventItem-17efdae2-c803-4411-aac9f6185bdf13de.html\",\n" +
                "        \"name\": \"test-name\",\n" +
                "        \"description\": \"test-description\",\n" +
                "        \"id\": \"17efdae2-c803-4411-aac9-f6185bdf13de\"\n" +
                "    },\n" +
                "    \"context\": {\n" +
                "        \"date\": 1342456531059,\n" +
                "        \"service\": \"rai.tv\",\n" +
                "        \"mood\": null,\n" +
                "        \"username\": \"dpalmisano\"\n" +
                "    }\n" +
                "}";
        String query = String.format(
                baseQuery,
                username,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(tokenManager.checkTokenExists(userToken)).thenReturn(true);

        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("activity", activity);
        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        assertFalse(responseBody.isEmpty());

        APIResponse actual = fromJson(responseBody, APIResponse.class);
        assertEquals(actual.getMessage(), "activity successfully registered");
        assertEquals(actual.getStatus(), "OK");
    }

    @Test
    public void testCustomActivityComment() throws Exception {
        String baseQuery = "activities/add/%s?token=%s";
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = user.getUserToken();
        String activity = "{\n" +
                "    \"verb\": \"COMMENT\",\n" +
                "    \"object\": {\n" +
                "        \"type\": \"RAI-TV-COMMENT\",\n" +
                "        \"url\": \"http://www.rai.tv/dl/RaiTV/programmi/media/Comment-17efdae2-c803-4411-aac9f6185bdf13de.html\",\n" +
                "        \"name\": \"test-name\",\n" +
                "        \"description\": \"test-description\",\n" +
                "        \"text\": \"this is a text of a comment\",\n" +
                "        \"inReplyTo\": \"17efdae2-c803-4411-aac9-f6185bdf13de\"\n" +
                "    },\n" +
                "    \"context\": {\n" +
                "        \"date\": 1342456531059,\n" +
                "        \"service\": \"rai.tv\",\n" +
                "        \"mood\": null,\n" +
                "        \"username\": \"dpalmisano\"\n" +
                "    }\n" +
                "}";
        String query = String.format(
                baseQuery,
                username,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(tokenManager.checkTokenExists(userToken)).thenReturn(true);

        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("activity", activity);
        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        assertFalse(responseBody.isEmpty());

        APIResponse actual = fromJson(responseBody, APIResponse.class);
        assertEquals(actual.getMessage(), "activity successfully registered");
        assertEquals(actual.getStatus(), "OK");
    }

    @Test
    public void testAddActivityObject() throws Exception {
        String baseQuery = "activities/add/%s?token=%s";
        String username = "test-user";
        User user = getUser(username);
        UUID userToken = user.getUserToken();
        String activity = "{\n" +
                "    \"verb\": \"WATCHED\",\n" +
                "    \"object\": {\n" +
                "        \"type\": \"OBJECT\",\n" +
                "        \"url\": \"http: //www.rai.tv/dl/RaiTV/socialtv/PublishingBlock-cab033f4-55b9-4a4d-b20f-6540a0ef5487.html\",\n" +
                "        \"name\": \"Miss Italia\",\n" +
                "        \"description\": \"Concorso Miss Italia\"\n" +
                "    },\n" +
                "    \"context\": {\n" +
                "        \"date\": 1340702105000,\n" +
                "        \"service\": null,\n" +
                "        \"mood\": null\n" +
                "    }\n" +
                "}";
        String query = String.format(
                baseQuery,
                username,
                userToken
        );

        when(userManager.getUser(username)).thenReturn(user);
        when(tokenManager.checkTokenExists(userToken)).thenReturn(true);

        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("activity", activity);
        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        assertFalse(responseBody.isEmpty());

        APIResponse actual = fromJson(responseBody, APIResponse.class);
        assertEquals(actual.getMessage(), "activity successfully registered");
        assertEquals(actual.getStatus(), "OK");
        assertNotNull(actual.getObject());
        assertNotNull(UUID.fromString(actual.getObject()));
    }

    private User getUser(String username) {
        User user = new User("Test", "User", username, "password");
        user.setUserToken(UUID.randomUUID());
        return user;
    }

    private ResolvedActivity createCustomActivity() throws Exception {
        Activity activity = new Activity();
        activity.setId(UUID.randomUUID());
        activity.setVerb(Verb.WATCHED);

        TVEvent tvEvent = new TVEvent(UUID.randomUUID(), "Euro 2012", "");

        Context context = new Context();
        context.setUsername("rai-username");
        context.setDate(new DateTime());
        context.setService("rai-tv");

        activity.setContext(context);
        activity.setObject(tvEvent);

        User user = getUser("rai-username");

        return new ResolvedActivity(user.getId(), activity, user);
    }

    private ResolvedActivity createActivity(int i) {
        Activity activity = new Activity();
        activity.setVerb(Verb.TWEET);
        activity.setId(UUID.randomUUID());

        Tweet tweet = new Tweet();
        tweet.setText("Fake text #" + i);

        Context context = new Context();
        context.setUsername("test-user");
        context.setDate(new DateTime().minusMinutes(i));
        context.setService("twitter");

        activity.setContext(context);
        activity.setObject(tweet);

        User user = getUser("test-user");

        return new ResolvedActivity(user.getId(), activity, user);
    }

    private Collection<ResolvedActivity> createSearchResults(int page, int size, String order) {
        int total = 50;
        int startAt = page * size;
        Collection<ResolvedActivity> results = new ArrayList<ResolvedActivity>();

        for (int i = startAt; i < startAt + size && i < total; i++) {
            results.add(createActivity((order.equals("desc"))
                    ? i
                    : total - 1 - i
            ));
        }

        return results;
    }

    public static class ActivitiesServiceTestConfig extends GuiceServletContextListener {
        @Override
        protected Injector getInjector() {
            return Guice.createInjector(new JerseyServletModule() {
                @Override
                protected void configureServlets() {
                    queues = mock(Queues.class);
                    activityStore = mock(ActivityStore.class);
                    userManager = mock(UserManager.class);
                    tokenManager = mock(UserTokenManager.class);
                    bind(ApplicationsManager.class).to(MockApplicationsManager.class).asEagerSingleton();
                    bind(ActivityStore.class).toInstance(activityStore);
                    bind(Queues.class).toInstance(queues);
                    bind(UserTokenManager.class).toInstance(tokenManager);
                    bind(UserManager.class).toInstance(userManager);

                    // add REST services
                    bind(ApplicationService.class);
                    bind(ActivitiesService.class);

                    // add bindings for Jackson
                    bind(JacksonJaxbJsonProvider.class).asEagerSingleton();
                    bind(JacksonMixInProvider.class).asEagerSingleton();
                    bind(MessageBodyReader.class).to(JacksonJsonProvider.class);
                    bind(MessageBodyWriter.class).to(JacksonJsonProvider.class);

                    // Route all requests through GuiceContainer
                    serve("/*").with(GuiceContainer.class);
                    filter("/*").through(GuiceContainer.class);
                }
            });
        }
    }
}