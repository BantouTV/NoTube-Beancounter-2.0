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
import io.beancounter.platform.AbstractJerseyTestCase;
import io.beancounter.platform.JacksonMixInProvider;
import io.beancounter.platform.responses.AtomicSignUpResponse;
import io.beancounter.usermanager.AtomicSignUp;
import io.beancounter.usermanager.UserManager;
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
        when(authHandler.authOnRai(username, password)).thenReturn(raiToken);
        when(tokenManager.createUserToken(username)).thenReturn(userToken);

        PostMethod postMethod = new PostMethod(base_uri + baseQuery);
        HttpClient client = new HttpClient();
        postMethod.addParameter("username", username);
        postMethod.addParameter("password", password);

        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        AtomicSignUpResponse response = fromJson(responseBody, AtomicSignUpResponse.class);
        assertEquals(response.getStatus(), AtomicSignUpResponse.Status.OK);
        assertEquals(response.getMessage(), "user with user name [" + username + "] logged in with service [" + SERVICE_NAME + "]");

        AtomicSignUp atomicSignUp = response.getObject();
        assertNotNull(atomicSignUp);
        assertEquals(atomicSignUp.getIdentifier(), username);
        assertEquals(atomicSignUp.getUsername(), username);
        assertFalse(atomicSignUp.isReturning());
        assertEquals(atomicSignUp.getService(), SERVICE_NAME);

        User user = userArgument.getValue();
        assertEquals(user.getUsername(), username);
        assertEquals(user.getServices().get(SERVICE_NAME).getSession(), raiToken);
        assertEquals(user.getUserToken(), userToken);
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

        AtomicSignUpResponse response = fromJson(responseBody, AtomicSignUpResponse.class);
        assertEquals(response.getStatus(), AtomicSignUpResponse.Status.OK);
        assertEquals(response.getMessage(), "user with user name [" + username + "] logged in with service [" + SERVICE_NAME + "]");

        AtomicSignUp atomicSignUp = response.getObject();
        assertNotNull(atomicSignUp);
        assertEquals(atomicSignUp.getIdentifier(), username);
        assertEquals(atomicSignUp.getUsername(), username);
        assertFalse(atomicSignUp.isReturning());
        assertEquals(atomicSignUp.getService(), SERVICE_NAME);

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