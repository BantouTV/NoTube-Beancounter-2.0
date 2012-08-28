package io.beancounter.platform.user;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import io.beancounter.applications.ApplicationsManager;
import io.beancounter.applications.MockApplicationsManager;
import io.beancounter.commons.helper.UriUtils;
import io.beancounter.commons.model.OAuthToken;
import io.beancounter.commons.model.User;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.ActivityBuilder;
import io.beancounter.commons.model.activity.DefaultActivityBuilder;
import io.beancounter.commons.model.activity.ResolvedActivity;
import io.beancounter.commons.model.activity.Tweet;
import io.beancounter.commons.model.activity.Verb;
import io.beancounter.commons.tests.Tests;
import io.beancounter.commons.tests.TestsBuilder;
import io.beancounter.platform.APIResponse;
import io.beancounter.platform.AbstractJerseyTestCase;
import io.beancounter.platform.ApplicationService;
import io.beancounter.platform.JacksonMixInProvider;
import io.beancounter.platform.PlatformResponse;
import io.beancounter.platform.UserService;
import io.beancounter.platform.responses.StringPlatformResponse;
import io.beancounter.platform.responses.UserPlatformResponse;
import io.beancounter.profiles.MockProfiles;
import io.beancounter.profiles.Profiles;
import io.beancounter.queues.Queues;
import io.beancounter.usermanager.AtomicSignUp;
import io.beancounter.usermanager.UserManager;
import io.beancounter.usermanager.UserManagerException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

/**
 * Reference test case for {@link io.beancounter.platform.UserService}
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class UserServiceTestCase extends AbstractJerseyTestCase {

    private Tests tests = TestsBuilder.getInstance().build();

    private static String APIKEY;
    private static UserManager userManager;
    private static Queues queues;

    protected UserServiceTestCase() {
        super(9995);
    }

    @Override
    protected void startFrontendService() throws IOException {
        server = new GrizzlyWebServer(9995);
        ServletAdapter ga = new ServletAdapter();
        ga.addServletListener(UserServiceTestConfig.class.getName());
        ga.setServletPath("/");
        ga.addFilter(new GuiceFilter(), "filter", null);
        server.addGrizzlyAdapter(ga, null);
        server.start();
    }

    @BeforeTest
    private void registerApplication() throws IOException {
        APIKEY = registerTestApplication().toString();
    }

    @AfterTest
    private void deregisterTestApplication() throws IOException {
        HttpClient client = new HttpClient();
        String baseQuery = "application/" + APIKEY;
        DeleteMethod deleteMethod = new DeleteMethod(base_uri + baseQuery);
        client.executeMethod(deleteMethod);
    }

    @BeforeMethod
    private void resetMocks() throws Exception {
        reset(userManager, queues);
    }

    private UUID registerTestApplication() throws IOException {
        String baseQuery = "application/register";
        final String name = "fake_application_name";
        final String description = "This is a test registration!";
        final String email = "fake_mail@test.com";
        final String oauth = "http://fakeUrlOAUTH";
        PostMethod postMethod = new PostMethod(base_uri + baseQuery);
        HttpClient client = new HttpClient();
        postMethod.addParameter("name", name);
        postMethod.addParameter("description", description);
        postMethod.addParameter("email", email);
        postMethod.addParameter("oauthCallback", oauth);
        client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        APIResponse actual = fromJson(responseBody, APIResponse.class);
        return UUID.fromString(actual.getObject());
    }

    @Test
    public void testSignUp() throws Exception {
        String baseQuery = "user/register?apikey=%s";
        String name = "Fake_Name";
        String surname = "Fake_Surname";
        String username = "missing-user";
        String password = "abc";
        String query = String.format(
                baseQuery,
                APIKEY
        );

        when(userManager.getUser(username)).thenReturn(null);

        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("name", name);
        postMethod.addParameter("surname", surname);
        postMethod.addParameter("username", username);
        postMethod.addParameter("password", password);
        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);

        assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        assertNotEquals(responseBody, "");
        APIResponse actual = fromJson(responseBody, APIResponse.class);
        APIResponse expected = new APIResponse(
                actual.getObject(),
                "user successfully registered",
                "OK"
        );
        assertEquals(actual, expected);
        assertNotNull(actual.getObject());
        assertNotNull(UUID.fromString(actual.getObject()));

        User user = new User(name, surname, username, password);
        verify(userManager).storeUser(user);
    }

    @Test
    public void testSignUpRandom() throws Exception {
        User user = tests.build(User.class).getObject();

        String baseQuery = "user/register?apikey=%s";
        String name = user.getName();
        String surname = user.getSurname();
        String username = "missing-user";
        String password = user.getPassword();
        String query = String.format(
                baseQuery,
                APIKEY
        );

        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("name", name);
        postMethod.addParameter("surname", surname);
        postMethod.addParameter("username", username);
        postMethod.addParameter("password", password);
        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);

        assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        assertNotEquals(responseBody, "");
        APIResponse actual = fromJson(responseBody, APIResponse.class);
        APIResponse expected = new APIResponse(
                actual.getObject(),
                "user successfully registered",
                "OK"
        );
        assertEquals(actual, expected);
        assertNotNull(actual.getObject());
        assertNotNull(UUID.fromString(actual.getObject()));
    }

    @Test
    public void signUpUserWithExistingUsernameShouldRespondWithError() throws Exception {
        String baseQuery = "user/register?apikey=%s";
        String name = "Fake_Name";
        String surname = "Fake_Surname";
        String username = "test-user";
        String password = "abc";
        String query = String.format(
                baseQuery,
                APIKEY
        );

        User user = new User("Test", "User", username, "password");
        when(userManager.getUser(username)).thenReturn(user);

        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("name", name);
        postMethod.addParameter("surname", surname);
        postMethod.addParameter("username", username);
        postMethod.addParameter("password", password);
        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);

        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR, "\"Unexpected result: [" + result + "]");
        assertNotEquals(responseBody, "");
        APIResponse actual = fromJson(responseBody, APIResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "username [test-user] is already taken",
                "NOK"
        );
        assertEquals(actual, expected);
    }

    @Test
    public void testGetUser() throws Exception {
        String baseQuery = "user/%s?apikey=%s";
        String username = "test-user";
        String query = String.format(
                baseQuery,
                username,
                APIKEY
        );

        User user = new User("Test", "User", username, "password");
        when(userManager.getUser(username)).thenReturn(user);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);

        assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        assertNotEquals(responseBody, "");

        UserPlatformResponse actual = fromJson(responseBody, UserPlatformResponse.class);
        assertNotNull(actual);
        assertEquals(actual.getMessage(), "user [" + username + "] found");
        assertEquals(actual.getStatus(), PlatformResponse.Status.OK);
        assertEquals(actual.getObject().getId(), user.getId());
        assertEquals(actual.getObject().getUsername(), user.getUsername());
        assertEquals(actual.getObject().getName(), user.getName());
        assertNull(actual.getObject().getPassword(), "The password should not be returned.");
    }
    
    @Test
    public void testGetUserMissingUser() throws Exception {
        String baseQuery = "user/%s?apikey=%s";
        String name = "missing-user";
        String query = String.format(
                baseQuery,
                name,
                APIKEY
        );

        when(userManager.getUser(name)).thenReturn(null);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        assertNotEquals(responseBody, "");
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR, "\"Unexpected result: [" + result + "]");
        APIResponse actual = fromJson(responseBody, APIResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "user with username [" + name + "] not found",
                "NOK"
        );
        assertEquals(actual, expected);
    }

    @Test
    public void testDeleteUser() throws Exception {
        String baseQuery = "user/%s?apikey=%s";
        String username = "test-user";
        String query = String.format(
                baseQuery,
                username,
                APIKEY
        );

        User user = new User("Test", "User", username, "password");
        when(userManager.getUser(username)).thenReturn(user);

        DeleteMethod deleteMethod = new DeleteMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(deleteMethod);
        String responseBody = new String(deleteMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        assertNotEquals(responseBody, "");
        assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        APIResponse actual = fromJson(responseBody, APIResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "user with username [" + username + "] deleted",
                "OK"
        );
        assertEquals(actual, expected);
    }

    @Test
    public void testAuthenticate() throws Exception {
        String baseQuery = "user/%s/authenticate?apikey=%s";
        String username = "test-user";
        String password = "abc";
        String query = String.format(
                baseQuery,
                username,
                APIKEY
        );

        User user = new User("Test", "User", username, password);
        when(userManager.getUser(username)).thenReturn(user);

        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("password", password);
        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        assertNotEquals(responseBody, "");
        assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        assertEquals(responseBody, "{\"status\":\"OK\",\"message\":\"user [test-user] authenticated\"}");
        APIResponse actual = fromJson(responseBody, APIResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "user [" + username + "] authenticated",
                "OK"
        );
        assertEquals(actual, expected);
    }

    @Test
    public void testGetOAuthToken() throws Exception {
        String baseQuery = "user/oauth/token/%s/%s?redirect=%s";
        String service = "fake-service-1";
        URL serviceRedirectUrl = new URL("http://example.com/fake-service/oauth/token-1234");
        String username = "test-user";
        String redirect = "http://testurl.com/";
        String query = String.format(
                baseQuery,
                service,
                username,
                redirect
        );

        User user = new User("Test", "User", username, "password");
        when(userManager.getUser(username)).thenReturn(user);
        when(userManager.getOAuthToken(service, username)).thenReturn(new OAuthToken(serviceRedirectUrl));

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        assertFalse(responseBody.isEmpty());
        assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");

        verify(userManager).setUserFinalRedirect(username, new URL(redirect));
    }

    @Test
    public void testHandleFacebookAuthCallback() throws Exception {
        String baseQuery = "user/oauth/callback/%s/%s?code=%s";
        String service = "facebook";
        String username = "test-user";
        String token = null;
        String code = "OAUTH-VERIFIER";
        String query = String.format(
                baseQuery,
                service,
                username,
                code
        );

        URL finalRedirect = new URL("http://example.com/final/redirect");
        User user = new User("Test", "User", username, "password");
        when(userManager.getUser(username)).thenReturn(user);
        when(userManager.consumeUserFinalRedirect(username)).thenReturn(finalRedirect);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);

        assertFalse(responseBody.isEmpty());
        assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");

        verify(userManager).registerOAuthService(service, user, token, code);
    }

    @Test
    public void testHandleOAuthCallback() throws Exception {
        String baseQuery = "user/oauth/callback/%s/%s?oauth_token=%s&oauth_verifier=%s";
        String service = "fake-service-1";
        String username = "test-user";
        String oauthToken = "OAUTH-TOKEN";
        String oauthVerifier = "OAUTH-VERIFIER";
        String query = String.format(
                baseQuery,
                service,
                username,
                oauthToken,
                oauthVerifier
        );

        URL finalRedirect = new URL("http://example.com/final/redirect");
        User user = new User("Test", "User", username, "password");
        when(userManager.getUser(username)).thenReturn(user);
        when(userManager.consumeUserFinalRedirect(username)).thenReturn(finalRedirect);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);

        assertFalse(responseBody.isEmpty());
        assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");

        verify(userManager).registerOAuthService(service, user, oauthToken, oauthVerifier);
    }

    @Test
    public void testHandleAuthCallback() throws Exception {
        String baseQuery = "user/auth/callback/%s/%s/%s?token=%s";
        String service = "fake-service-1";
        String username = "test-user";
        String redirect = "testurl.com";
        String token = "TOKEN";
        String query = String.format(
                baseQuery,
                service,
                username,
                redirect,
                token
        );

        User user = new User("Test", "User", username, "password");
        when(userManager.getUser(username)).thenReturn(user);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);

        assertEquals(responseBody, "");
        assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");

        verify(userManager).registerService(service, user, token);
    }

    @Test
    public void testRemoveSource() throws Exception {
        String baseQuery = "user/source/%s/%s?apikey=%s";
        String username = "test-user";
        String service = "fake-service-1";
        String query = String.format(
                baseQuery,
                username,
                service,
                APIKEY
        );

        User user = new User("Test", "User", username, "password");
        when(userManager.getUser(username)).thenReturn(user);

        DeleteMethod deleteMethod = new DeleteMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(deleteMethod);
        String responseBody = new String(deleteMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);

        assertNotEquals(responseBody, "");
        assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        APIResponse actual = fromJson(responseBody, APIResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "service [" + service + "] removed from user [" + username + "]",
                "OK"
        );
        assertEquals(actual, expected);

        verify(userManager).deregisterService(service, user);
    }

    @Test
    public void testRemoveSourceWithNotExistingUser() throws Exception {
        String baseQuery = "user/source/%s/%s?apikey=%s";
        String username = "missing-user";
        String service = "fake-service-1";
        String query = String.format(
                baseQuery,
                username,
                service,
                APIKEY
        );

        when(userManager.getUser(username)).thenReturn(null);

        DeleteMethod deleteMethod = new DeleteMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(deleteMethod);
        String responseBody = new String(deleteMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        assertNotEquals(responseBody, "");
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR, "\"Unexpected result: [" + result + "]");
        APIResponse actual = fromJson(responseBody, APIResponse.class);
        APIResponse expected = new APIResponse(
                null,
                "User [" + username + "] not found!",
                "NOK"
        );
        assertEquals(actual, expected);
    }

    // TODO (mid) review this test, we need to store the profile first
    @Test(enabled = false)
    public void testGetProfile() throws IOException {
        String baseQuery = "user/%s/profile?apikey=%s";
        String username = "test-user";
        String query = String.format(
                baseQuery,
                username,
                APIKEY
        );
        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);
        assertNotEquals(responseBody, "");
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR, "\"Unexpected result: [" + result + "]");
    }

    @Test
    public void signingUpWithServiceFromMobileDeviceShouldRedirectWithoutErrors() throws Exception {
        String baseQuery = "user/register/%s/mobile";
        String service = "social-service";
        String query = String.format(
                baseQuery,
                service
        );

        URL redirectUrl = new URL("http://example.com/oauth/token-1234");
        when(userManager.getOAuthToken(service)).thenReturn(new OAuthToken(redirectUrl));

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);

        assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        assertFalse(responseBody.isEmpty());
        assertEquals(getMethod.getURI().getHost(), "www.iana.org");
    }

    @Test
    public void signingUpWithServiceFromMobileDeviceWhenNoOAuthTokenCanBeRetrievedShouldRespondWithError() throws Exception {
        String baseQuery = "user/register/%s/mobile";
        String service = "social-service";
        String errorMessage = "Error getting OAuth token";
        String query = String.format(
                baseQuery,
                service
        );

        when(userManager.getOAuthToken(service))
                .thenThrow(new UserManagerException(errorMessage));

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);

        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR, "\"Unexpected result: [" + result + "]");
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse actual = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(actual.getStatus(), PlatformResponse.Status.NOK);
        assertEquals(actual.getMessage(), "Error while getting token from [" + service + "]");
        assertEquals(actual.getObject(), errorMessage);
    }

    @Test
    public void signingUpWithServiceFromTheWebShouldRedirectWithoutErrors() throws Exception {
        String baseQuery = "user/register/%s/web?redirect=%s";
        String service = "social-service";
        String finalRedirect = "http://example.com";
        String query = String.format(
                baseQuery,
                service,
                finalRedirect
        );

        URL redirectUrl = new URL("http://example.com/oauth/token-1234");
        when(userManager.getOAuthToken(service, new URL(finalRedirect)))
                .thenReturn(new OAuthToken(redirectUrl));

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);

        assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        assertFalse(responseBody.isEmpty());
        assertEquals(getMethod.getURI().getHost(), "www.iana.org");
    }

    @Test
    public void signingUpWithServiceFromTheWebWhenNoOAuthTokenCanBeRetrievedShouldRespondWithError() throws Exception {
        String baseQuery = "user/register/%s/web?redirect=%s";
        String service = "social-service";
        String finalRedirect = "http://example.com";
        String errorMessage = "Error getting OAuth token";
        String query = String.format(
                baseQuery,
                service,
                finalRedirect
        );

        when(userManager.getOAuthToken(service, new URL(finalRedirect)))
                .thenThrow(new UserManagerException(errorMessage));

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);

        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR, "\"Unexpected result: [" + result + "]");
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse actual = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(actual.getStatus(), PlatformResponse.Status.NOK);
        assertEquals(actual.getMessage(), "Error while getting token from [" + service + "]");
        assertEquals(actual.getObject(), errorMessage);
    }

    @Test
    public void signingUpWithServiceFromTheWebWhenMissingFinalRedirectParamShouldRespondWithError() throws Exception {
        String baseQuery = "user/register/%s/web";
        String service = "social-service";
        String query = String.format(
                baseQuery,
                service
        );

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);

        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR, "\"Unexpected result: [" + result + "]");
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse actual = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(actual.getStatus(), PlatformResponse.Status.NOK);
        assertEquals(actual.getMessage(), "[null] is not a valid URL");
    }

    @Test
    public void handlingAtomicTwitterOAuthCallbackFromWebShouldRedirectWithoutErrors() throws Exception {
        String baseQuery = "user/oauth/atomic/callback/%s/web/%s?oauth_token=%s&oauth_verifier=%s";
        String service = "twitter";
        String serviceUserId = "1234564321";
        String username = "test-user";
        String token = "twitter-oauth-token";
        String verifier = "twitter-oauth-verifier";
        String decodedFinalRedirectUrl = "http://example.com/final/redirect";
        String encodedFinalRedirectUrl = UriUtils.encodeBase64(decodedFinalRedirectUrl);
        String query = String.format(
                baseQuery,
                service,
                encodedFinalRedirectUrl,
                token,
                verifier
        );

        User user = new User("Test", "User", username, "password");
        AtomicSignUp signUp = new AtomicSignUp(user.getId(), username, false, service, serviceUserId);
        List<Activity> activities = generateActivities(service, serviceUserId, 3);

        when(userManager.storeUserFromOAuth(service, token, verifier, decodedFinalRedirectUrl))
                .thenReturn(signUp);
        when(userManager.getUser(username)).thenReturn(user);
        when(userManager.grabUserActivities(user, serviceUserId, service, 40))
                .thenReturn(activities);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();
        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        logger.info("result code: " + result);
        logger.info("response body: " + responseBody);

        assertEquals(result, HttpStatus.SC_OK, "\"Unexpected result: [" + result + "]");
        assertFalse(responseBody.isEmpty());
        assertEquals(getMethod.getURI().getHost(), "www.iana.org");
        /*
        ObjectMapper mapper = new ObjectMapper();
        for (Activity activity : activities) {
            ResolvedActivity ra = new ResolvedActivity();
            ra.setActivity(activity);
            ra.setUserId(user.getId());
            ra.setUser(user);

            verify(queues).push(mapper.writeValueAsString(ra));
        }
        */
        verify(userManager).storeUserFromOAuth(service, token, verifier, decodedFinalRedirectUrl);
    }

    private List<Activity> generateActivities(
            String service,
            String serviceUserId,
            int numActivities
    ) throws Exception {
        List<Activity> activities = new ArrayList<Activity>(numActivities);
        ActivityBuilder builder = new DefaultActivityBuilder();

        for (int i = 0; i < numActivities; i++) {
            builder.push();
            builder.setVerb(Verb.TWEET);
            Map<String, Object> fields = new HashMap<String, Object>();
            fields.put("setText", "Fake tweet number " + (i + 1));
            builder.setObject(
                    Tweet.class,
                    new URL("http://twitter.com/status/1237281" + i),
                    "Tweet Name",
                    fields
            );
            builder.setContext(new DateTime(), service, serviceUserId);
            activities.add(builder.pop());
        }

        return activities;
    }

    public static class UserServiceTestConfig extends GuiceServletContextListener {
        @Override
        protected Injector getInjector() {
            return Guice.createInjector(new JerseyServletModule() {
                @Override
                protected void configureServlets() {
                    userManager = mock(UserManager.class);
                    queues = mock(Queues.class);
                    bind(ApplicationsManager.class).to(MockApplicationsManager.class).asEagerSingleton();
                    bind(UserManager.class).toInstance(userManager);
                    bind(Profiles.class).to(MockProfiles.class);
                    bind(Queues.class).toInstance(queues);

                    // add REST services
                    bind(ApplicationService.class);
                    bind(UserService.class);

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