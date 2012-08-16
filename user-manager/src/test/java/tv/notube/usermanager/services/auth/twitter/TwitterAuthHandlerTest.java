package tv.notube.usermanager.services.auth.twitter;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import tv.notube.commons.helper.PropertiesHelper;
import tv.notube.commons.helper.jedis.JedisPoolFactory;
import tv.notube.commons.model.OAuthToken;
import tv.notube.commons.model.Service;
import tv.notube.commons.model.User;
import tv.notube.commons.model.auth.AuthHandler;
import tv.notube.commons.model.auth.AuthHandlerException;
import tv.notube.commons.model.auth.AuthenticatedUser;
import tv.notube.commons.model.auth.OAuthAuth;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import java.net.MalformedURLException;
import java.net.URL;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class TwitterAuthHandlerTest {

    private static final String ACCESS_TOKEN = "7588892-kagSNqWge8gB1WwE3plnFsJHAZVfxWD7Vb57p0b4";
    private static final String ACCESS_TOKEN_SECRET = "PbKfYqSryyeKDWz4ebtY3o5ogNLG11WJuZBc9fQrQo";
    private static final String ATOMIC_OAUTH_CALLBACK = "http://api.beancounter.io/rest/user/oauth/atomic/callback/twitter/";
    private static final String OAUTH_CALLBACK = "http://api.beancounter.io/rest/user/oauth/callback/twitter/";
    private static final String REQUEST_TOKEN = "twitter-request-token";
    private static final String REDIRECT_URL = "https://api.twitter.com/oauth/authenticate?oauth_token=" + REQUEST_TOKEN;
    private static final String USERNAME = "test-user";

    private AuthHandler twitterHandler;
    private OAuthService twitterOAuth;
    private ServiceBuilder serviceBuilder;
    private Jedis jedis;
    private Twitter twitter;

    @BeforeMethod
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new Module() {
            @Override
            public void configure(Binder binder) {
                Names.bindProperties(binder, PropertiesHelper.readFromClasspath("/redis.properties"));

                Service service = new Service("twitter");

                try {
                    service.setDescription("Twitter service");
                    service.setEndpoint(new URL("https://api.twitter.com/1/statuses/user_timeline.json"));
                    service.setSessionEndpoint(new URL("https://api.twitter.com/oauth/request_token"));
                    service.setApikey("twitter-api-key");
                    service.setSecret("twitter-secret");
                    service.setOAuthCallback(new URL(OAUTH_CALLBACK));
                    service.setAtomicOAuthCallback(new URL(ATOMIC_OAUTH_CALLBACK));
                } catch (MalformedURLException mue) {
                    fail();
                }

                twitterOAuth = mock(OAuthService.class);
                serviceBuilder = mock(ServiceBuilder.class);
                when(serviceBuilder.callback(anyString())).thenReturn(serviceBuilder);
                when(serviceBuilder.build()).thenReturn(twitterOAuth);

                jedis = mock(Jedis.class);
                JedisPool jedisPool = mock(JedisPool.class);
                JedisPoolFactory jedisPoolFactory = mock(JedisPoolFactory.class);
                when(jedisPoolFactory.build()).thenReturn(jedisPool);
                when(jedisPool.getResource()).thenReturn(jedis);

                twitter = mock(Twitter.class);
                TwitterFactoryWrapper twitterFactory = mock(TwitterFactoryWrapper.class);
                when(twitterFactory.getInstance()).thenReturn(twitter);

                binder.bind(Service.class).toInstance(service);
                binder.bind(ServiceBuilder.class).toInstance(serviceBuilder);
                binder.bind(JedisPoolFactory.class).toInstance(jedisPoolFactory);
                binder.bind(TwitterFactory.class);
                binder.bind(TwitterFactoryWrapper.class).toInstance(twitterFactory);
                binder.bind(AuthHandler.class).to(TwitterAuthHandler.class);
            }
        });

        twitterHandler = injector.getInstance(AuthHandler.class);
    }

    @Test
    public void twitterHandlerShouldHaveCorrectServiceType() throws Exception {
        assertEquals(twitterHandler.getService(), "twitter");
    }

    @Test(expectedExceptions = AuthHandlerException.class)
    public void twitterAuthHandlerShouldNotAuthenticateWhenAVerifierIsNotProvided() throws Exception {
        User user = new User("Test", "User", USERNAME, "password");
        twitterHandler.auth(user, "token-but-no-verifier");
    }

    @Test(expectedExceptions = AuthHandlerException.class)
    public void twitterAuthHandlerShouldNotAuthenticateWhenOnlyAVerifierIsProvided() throws Exception {
        twitterHandler.auth("only-a-verifier");
    }

    @Test(expectedExceptions = AuthHandlerException.class)
    public void twitterAuthHandlerShouldNotAuthenticateWhenNoTokenIsProvided() throws Exception {
        twitterHandler.auth("only-a-verifier", "http://final.redirect.url.com");
    }

    @Test
    public void twitterOAuthTokenShouldHaveCorrectRedirectUrl() throws Exception {
        initTokenMocks(ATOMIC_OAUTH_CALLBACK);

        OAuthToken oAuthToken = twitterHandler.getToken();
        assertEquals(oAuthToken.getRedirectPage().toString(), REDIRECT_URL);
    }

    @Test
    public void twitterOAuthTokenShouldHaveCorrectRedirectUrlWhenFinalRedirectUrlIsSpecified() throws Exception {
        initTokenMocks(ATOMIC_OAUTH_CALLBACK + "web/http%3A%2F%2Fapi.beancounter.io%2Ffinal%2Fredirect");

        OAuthToken oAuthToken = twitterHandler.getToken(new URL("http://api.beancounter.io/final/redirect"));
        assertEquals(oAuthToken.getRedirectPage().toString(), REDIRECT_URL);
    }

    @Test
    public void twitterOAuthTokenShouldHaveCorrectRedirectUrlWhenUsernameIsSpecified() throws Exception {
        initTokenMocks(OAUTH_CALLBACK + USERNAME);

        OAuthToken oAuthToken = twitterHandler.getToken(USERNAME);
        assertEquals(oAuthToken.getRedirectPage().toString(), REDIRECT_URL);
    }

    @Test
    public void twitterOAuthTokenShouldHaveCorrectRedirectUrlWhenUsernameAndCustomCallbackAreSpecified() throws Exception {
        String customCallback = "http://api.beancounter.io/my/custom/callback/";
        initTokenMocks(customCallback + USERNAME);

        OAuthToken oAuthToken = twitterHandler.getToken(USERNAME, new URL(customCallback));
        assertEquals(oAuthToken.getRedirectPage().toString(), REDIRECT_URL);
    }

    @Test
    public void authUserWithValidTokenAndVerifier() throws Exception {
        Token accessToken = new Token(ACCESS_TOKEN, ACCESS_TOKEN_SECRET);
        when(jedis.get(REQUEST_TOKEN)).thenReturn("twitter-request-token-secret");
        when(twitterOAuth.getAccessToken(Matchers.<Token>any(), Matchers.<Verifier>any())).thenReturn(accessToken);

        twitter4j.User twitterUser = mock(twitter4j.User.class);
        when(twitterUser.getId()).thenReturn(12345654321L);
        when(twitter.verifyCredentials()).thenReturn(twitterUser);

        User user = new User("Test", "User", USERNAME, "password");
        AuthenticatedUser authenticatedUser = twitterHandler.auth(user, REQUEST_TOKEN, "twitter-verifier");

        assertEquals(authenticatedUser.getUserId(), "12345654321");
        assertEquals(authenticatedUser.getUser().getName(), user.getName());
        assertEquals(authenticatedUser.getUser().getSurname(), user.getSurname());
        assertEquals(authenticatedUser.getUser().getUsername(), user.getUsername());
        assertEquals(authenticatedUser.getUser().getPassword(), user.getPassword());
        assertEquals(authenticatedUser.getUser().getServices().size(), 1);

        OAuthAuth auth = (OAuthAuth) authenticatedUser.getUser().getAuth(twitterHandler.getService());
        assertEquals(auth.getSession(), accessToken.getToken());
        assertEquals(auth.getSecret(), accessToken.getSecret());
    }

    @Test(expectedExceptions = AuthHandlerException.class)
    public void authUserWithValidTokenAndVerifierButCannotConnectToTwitter() throws Exception {
        Token accessToken = new Token(ACCESS_TOKEN, ACCESS_TOKEN_SECRET);
        when(jedis.get(REQUEST_TOKEN)).thenReturn("twitter-request-token-secret");
        when(twitterOAuth.getAccessToken(Matchers.<Token>any(), Matchers.<Verifier>any())).thenReturn(accessToken);
        when(twitter.verifyCredentials()).thenThrow(new TwitterException("Network problem"));

        User user = new User("Test", "User", USERNAME, "password");
        twitterHandler.auth(user, REQUEST_TOKEN, "twitter-verifier");
    }

    @Test(expectedExceptions = AuthHandlerException.class)
    public void authUserWithInvalidAccessToken() throws Exception {
        Token accessToken = new Token(ACCESS_TOKEN, ACCESS_TOKEN_SECRET);
        TwitterException exception = new TwitterException(
                "OAuth credentials are incorrect.",
                new Exception(),
                401
        );
        when(jedis.get(REQUEST_TOKEN)).thenReturn("twitter-request-token-secret");
        when(twitterOAuth.getAccessToken(Matchers.<Token>any(), Matchers.<Verifier>any())).thenReturn(accessToken);
        when(twitter.verifyCredentials()).thenThrow(exception);

        User user = new User("Test", "User", USERNAME, "password");
        twitterHandler.auth(user, REQUEST_TOKEN, "twitter-verifier");
    }

    @Test(expectedExceptions = AuthHandlerException.class)
    public void authUserWithExpiredRequestToken() throws Exception {
        when(jedis.get(REQUEST_TOKEN)).thenReturn(null);

        User user = new User("Test", "User", USERNAME, "password");
        twitterHandler.auth(user, REQUEST_TOKEN, "twitter-verifier");
    }

    private void initTokenMocks(String callback) {
        TwitterApi.Authenticate twitterApi = new TwitterApi.Authenticate();
        Token token = new Token(REQUEST_TOKEN, null);
        when(twitterOAuth.getRequestToken()).thenReturn(token);
        when(twitterOAuth.getAuthorizationUrl(token)).thenReturn(twitterApi.getAuthorizationUrl(token));
        when(serviceBuilder.callback(anyString())).thenAnswer(assertCallbackEquals(callback));
    }

    private Answer<Object> assertCallbackEquals(final String expected) {
        return new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String callback = (String) invocation.getArguments()[0];
                assertEquals(callback, expected);

                return serviceBuilder;
            }
        };
    }
}
