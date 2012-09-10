package io.beancounter.platform.rai;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import io.beancounter.commons.model.User;
import io.beancounter.commons.model.auth.SimpleAuth;
import io.beancounter.platform.AbstractJerseyTestCase;
import io.beancounter.platform.JacksonMixInProvider;
import io.beancounter.platform.responses.MyRaiTVSignUpResponse;
import io.beancounter.platform.responses.StringPlatformResponse;
import io.beancounter.usermanager.UserManager;
import io.beancounter.usermanager.UserManagerException;
import io.beancounter.usermanager.UserTokenManager;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.HttpStatus;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Reference test case for {@link io.beancounter.platform.rai.MyRaiTVService}
 */
public class MyRaiTVServiceTestCase extends AbstractJerseyTestCase {

    private static final String SERVICE_NAME = "myRai";

    private static UserManager userManager;
    private static UserTokenManager tokenManager;
    private static MyRaiTVAuthHandler authHandler;

    protected MyRaiTVServiceTestCase() {
        super(9995);
    }

    @Override
    protected void startFrontendService() throws IOException {
        server = new GrizzlyWebServer(9995);
        ServletAdapter ga = new ServletAdapter();
        ga.addServletListener(MyRaiTVServiceTestConfig.class.getName());
        ga.setServletPath("/");
        ga.addFilter(new GuiceFilter(), "filter", null);
        server.addGrizzlyAdapter(ga, null);
        server.start();
    }

    @BeforeMethod
    public void resetMocks() throws Exception {
        reset(userManager, tokenManager, authHandler);
    }

    @Test
    public void loginWithNewUserShouldCreateUser() throws Exception {
        String baseQuery = "rai/login";
        String username = "username";
        String password = "password";
        String raiToken = "myRai-token";
        UUID userToken = UUID.randomUUID();

        ArgumentCaptor<User> userArgument = ArgumentCaptor.forClass(User.class);
        when(userManager.getUser(username)).thenReturn(null);
        doNothing().when(userManager).storeUser(userArgument.capture());
        when(authHandler.authOnRai(username, password)).thenReturn(new MyRaiTVAuthResponse(raiToken, username));
        when(tokenManager.createUserToken(username)).thenReturn(userToken);

        PostMethod postMethod = new PostMethod(base_uri + baseQuery);
        HttpClient client = new HttpClient();
        postMethod.addParameter("username", username);
        postMethod.addParameter("password", password);

        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        MyRaiTVSignUpResponse response = fromJson(responseBody, MyRaiTVSignUpResponse.class);
        assertEquals(response.getStatus(), MyRaiTVSignUpResponse.Status.OK);
        assertEquals(response.getMessage(), "user with user name [" + username + "] logged in with service [" + SERVICE_NAME + "]");

        MyRaiTVSignUp signUp = response.getObject();
        assertNotNull(signUp);
        assertEquals(signUp.getIdentifier(), username);
        assertEquals(signUp.getUsername(), username);
        assertFalse(signUp.isReturning());
        assertEquals(signUp.getService(), SERVICE_NAME);
        assertEquals(signUp.getToken(), raiToken);

        User user = userArgument.getValue();
        assertEquals(user.getUsername(), username);
        assertEquals(user.getServices().get(SERVICE_NAME).getSession(), raiToken);
        assertEquals(user.getUserToken(), userToken);
    }

    @Test
    public void loginWithExistingUserShouldUpdateAuthTokenAndUserToken() throws Exception {
        String baseQuery = "rai/login";
        String username = "username";
        String password = "password";
        String oldRaiToken = "old-myRai-token";
        String newRaiToken = "new-myRai-token";
        UUID oldUserToken = UUID.randomUUID();
        UUID newUserToken = UUID.randomUUID();

        User oldUser = new User();
        oldUser.setUsername(username);
        oldUser.addService(SERVICE_NAME, new SimpleAuth(oldRaiToken, username));
        oldUser.setUserToken(oldUserToken);

        ArgumentCaptor<User> userArgument = ArgumentCaptor.forClass(User.class);
        when(userManager.getUser(username)).thenReturn(oldUser);
        doNothing().when(userManager).storeUser(userArgument.capture());
        when(authHandler.authOnRai(username, password)).thenReturn(new MyRaiTVAuthResponse(newRaiToken, username));
        when(tokenManager.createUserToken(username)).thenReturn(newUserToken);

        PostMethod postMethod = new PostMethod(base_uri + baseQuery);
        HttpClient client = new HttpClient();
        postMethod.addParameter("username", username);
        postMethod.addParameter("password", password);

        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        MyRaiTVSignUpResponse response = fromJson(responseBody, MyRaiTVSignUpResponse.class);
        assertEquals(response.getStatus(), MyRaiTVSignUpResponse.Status.OK);
        assertEquals(response.getMessage(), "user with user name [" + username + "] logged in with service [" + SERVICE_NAME + "]");

        MyRaiTVSignUp signUp = response.getObject();
        assertNotNull(signUp);
        assertEquals(signUp.getIdentifier(), username);
        assertEquals(signUp.getUsername(), username);
        assertTrue(signUp.isReturning());
        assertEquals(signUp.getService(), SERVICE_NAME);
        assertEquals(signUp.getToken(), newRaiToken);

        User user = userArgument.getValue();
        assertEquals(user.getUsername(), username);
        assertEquals(user.getServices().get(SERVICE_NAME).getSession(), newRaiToken);
        assertEquals(user.getUserToken(), newUserToken);

        verify(tokenManager).deleteUserToken(oldUserToken);
        verify(tokenManager).createUserToken(username);
    }

    @Test
    public void loginWithNewUserShouldUseTheCaseInsensitiveRaiTvUsernameRatherThanUserInput() throws Exception {
        String baseQuery = "rai/login";
        String inputUsername = "UseRnAmE";
        String raiUsername = "username";
        String password = "password";
        String raiToken = "myRai-token";
        UUID userToken = UUID.randomUUID();

        ArgumentCaptor<User> userArgument = ArgumentCaptor.forClass(User.class);
        when(authHandler.authOnRai(inputUsername, password)).thenReturn(new MyRaiTVAuthResponse(raiToken, raiUsername));
        when(userManager.getUser(raiUsername)).thenReturn(null);
        doNothing().when(userManager).storeUser(userArgument.capture());
        when(tokenManager.createUserToken(raiUsername)).thenReturn(userToken);

        PostMethod postMethod = new PostMethod(base_uri + baseQuery);
        HttpClient client = new HttpClient();
        postMethod.addParameter("username", inputUsername);
        postMethod.addParameter("password", password);

        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        MyRaiTVSignUpResponse response = fromJson(responseBody, MyRaiTVSignUpResponse.class);
        assertEquals(response.getStatus(), MyRaiTVSignUpResponse.Status.OK);
        assertEquals(response.getMessage(), "user with user name [" + raiUsername + "] logged in with service [" + SERVICE_NAME + "]");

        MyRaiTVSignUp signUp = response.getObject();
        assertNotNull(signUp);
        assertEquals(signUp.getIdentifier(), raiUsername);
        assertEquals(signUp.getUsername(), raiUsername);
        assertFalse(signUp.isReturning());
        assertEquals(signUp.getService(), SERVICE_NAME);
        assertEquals(signUp.getToken(), raiToken);

        User user = userArgument.getValue();
        assertEquals(user.getUsername(), raiUsername);
        assertEquals(user.getServices().get(SERVICE_NAME).getSession(), raiToken);
        assertEquals(user.getUserToken(), userToken);
    }

    @Test
    public void loginWithAuthWithExistingUserShouldBeCaseInsensitive() throws Exception {
        String baseQuery = "rai/login/auth";
        String inputUsername = "UseRnAmE";
        String raiUsername = "username";
        String oldRaiToken = "old-myRai-token";
        String newRaiToken = "new-myRai-token";
        UUID oldUserToken = UUID.randomUUID();
        UUID newUserToken = UUID.randomUUID();

        User oldUser = new User();
        oldUser.setUsername(raiUsername);
        oldUser.addService(SERVICE_NAME, new SimpleAuth(oldRaiToken, raiUsername));
        oldUser.setUserToken(oldUserToken);

        ArgumentCaptor<User> userArgument = ArgumentCaptor.forClass(User.class);
        when(userManager.getUser(raiUsername)).thenReturn(oldUser);
        doNothing().when(userManager).storeUser(userArgument.capture());
        when(tokenManager.createUserToken(raiUsername)).thenReturn(newUserToken);

        PostMethod postMethod = new PostMethod(base_uri + baseQuery);
        HttpClient client = new HttpClient();
        postMethod.addParameter("username", inputUsername);
        postMethod.addParameter("token", newRaiToken);

        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        MyRaiTVSignUpResponse response = fromJson(responseBody, MyRaiTVSignUpResponse.class);
        assertEquals(response.getStatus(), MyRaiTVSignUpResponse.Status.OK);
        assertEquals(response.getMessage(), "user with user name [" + raiUsername + "] logged in with service [" + SERVICE_NAME + "]");

        MyRaiTVSignUp signUp = response.getObject();
        assertNotNull(signUp);
        assertEquals(signUp.getIdentifier(), raiUsername);
        assertEquals(signUp.getUsername(), raiUsername);
        assertTrue(signUp.isReturning());
        assertEquals(signUp.getService(), SERVICE_NAME);
        assertEquals(signUp.getToken(), newRaiToken);

        User user = userArgument.getValue();
        assertEquals(user.getUsername(), raiUsername);
        assertEquals(user.getServices().get(SERVICE_NAME).getSession(), newRaiToken);
        assertEquals(user.getUserToken(), newUserToken);

        verify(tokenManager).deleteUserToken(oldUserToken);
        verify(tokenManager).createUserToken(raiUsername);
    }

    @Test
    public void givenErrorOccursWhenStoringUpdatesToExistingUserThenRespondWithError() throws Exception {
        String baseQuery = "rai/login";
        String username = "username";
        String password = "password";
        String oldRaiToken = "old-myRai-token";
        String newRaiToken = "new-myRai-token";
        UUID oldUserToken = UUID.randomUUID();

        User oldUser = new User();
        oldUser.setUsername(username);
        oldUser.addService(SERVICE_NAME, new SimpleAuth(oldRaiToken, username));
        oldUser.setUserToken(oldUserToken);

        when(userManager.getUser(username)).thenReturn(oldUser);
        when(authHandler.authOnRai(username, password)).thenReturn(new MyRaiTVAuthResponse(newRaiToken, username));
        when(tokenManager.createUserToken(username)).thenThrow(new UserManagerException("error"));

        PostMethod postMethod = new PostMethod(base_uri + baseQuery);
        HttpClient client = new HttpClient();
        postMethod.addParameter("username", username);
        postMethod.addParameter("password", password);

        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "error while storing user [" + username + "] on beancounter.io");
    }

    @Test
    public void loginWithNewUserUsingInvalidCredentialsShouldRespondWithError() throws Exception {
        String baseQuery = "rai/login";
        String username = "invalid-username";
        String password = "invalid-password";
        String expectedMessage = "user [" + username + "] is not authorized from myRai auth service";

        when(authHandler.authOnRai(username, password)).thenThrow(new MyRaiTVAuthException(expectedMessage));

        PostMethod postMethod = new PostMethod(base_uri + baseQuery);
        HttpClient client = new HttpClient();
        postMethod.addParameter("username", username);
        postMethod.addParameter("password", password);

        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), expectedMessage);
    }

    @Test
    public void givenErrorOccursWhenAuthenticatingNewUserThenRespondWithError() throws Exception {
        String baseQuery = "rai/login";
        String username = "invalid-username";
        String password = "invalid-password";

        when(authHandler.authOnRai(username, password)).thenThrow(new IOException());

        PostMethod postMethod = new PostMethod(base_uri + baseQuery);
        HttpClient client = new HttpClient();
        postMethod.addParameter("username", username);
        postMethod.addParameter("password", password);

        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "Error while authenticating [" + username + "] on myRai auth service");
    }

    @Test
    public void givenUserManagerErrorOccursWhenLookingUpNewUserDuringLoginThenRespondWithError() throws Exception {
        String baseQuery = "rai/login";
        String username = "unknown-username";
        String password = "password";
        String raiToken = "myRai-token";

        when(authHandler.authOnRai(username, password)).thenReturn(new MyRaiTVAuthResponse(raiToken, username));
        when(userManager.getUser(username)).thenThrow(new UserManagerException("error"));

        PostMethod postMethod = new PostMethod(base_uri + baseQuery);
        HttpClient client = new HttpClient();
        postMethod.addParameter("username", username);
        postMethod.addParameter("password", password);

        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "Error while getting beancounter.io user with name [" + username + "]");
    }

    @Test
    public void loginWithAuthWithNewUserShouldCreateUser() throws Exception {
        String baseQuery = "rai/login/auth";
        String username = "username";
        String raiToken = "myRai-token";
        UUID userToken = UUID.randomUUID();

        ArgumentCaptor<User> userArgument = ArgumentCaptor.forClass(User.class);
        when(userManager.getUser(username)).thenReturn(null);
        doNothing().when(userManager).storeUser(userArgument.capture());
        when(tokenManager.createUserToken(username)).thenReturn(userToken);

        PostMethod postMethod = new PostMethod(base_uri + baseQuery);
        HttpClient client = new HttpClient();
        postMethod.addParameter("username", username);
        postMethod.addParameter("token", raiToken);

        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        MyRaiTVSignUpResponse response = fromJson(responseBody, MyRaiTVSignUpResponse.class);
        assertEquals(response.getStatus(), MyRaiTVSignUpResponse.Status.OK);
        assertEquals(response.getMessage(), "user with user name [" + username + "] logged in with service [" + SERVICE_NAME + "]");

        MyRaiTVSignUp signUp = response.getObject();
        assertNotNull(signUp);
        assertEquals(signUp.getIdentifier(), username);
        assertEquals(signUp.getUsername(), username);
        assertFalse(signUp.isReturning());
        assertEquals(signUp.getService(), SERVICE_NAME);
        assertEquals(signUp.getToken(), raiToken);

        User user = userArgument.getValue();
        assertEquals(user.getUsername(), username);
        assertEquals(user.getServices().get(SERVICE_NAME).getSession(), raiToken);
        assertEquals(user.getUserToken(), userToken);
    }

    public static class MyRaiTVServiceTestConfig extends GuiceServletContextListener {
        @Override
        protected Injector getInjector() {
            return Guice.createInjector(new JerseyServletModule() {
                @Override
                protected void configureServlets() {
                    userManager = mock(UserManager.class);
                    tokenManager = mock(UserTokenManager.class);
                    authHandler = mock(MyRaiTVAuthHandler.class);
                    bind(UserManager.class).toInstance(userManager);
                    bind(UserTokenManager.class).toInstance(tokenManager);
                    bind(MyRaiTVAuthHandler.class).toInstance(authHandler);
                    bind(MyRaiTVService.class);

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