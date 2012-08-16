package tv.notube.usermanager.services.auth.twitter;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import tv.notube.commons.helper.jedis.JedisPoolFactory;
import tv.notube.commons.model.*;
import tv.notube.commons.model.User;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.auth.AuthHandlerException;
import tv.notube.commons.model.auth.AuthenticatedUser;
import tv.notube.commons.model.auth.DefaultAuthHandler;
import tv.notube.commons.model.OAuthToken;
import tv.notube.commons.model.auth.OAuthAuth;
import twitter4j.*;
import twitter4j.auth.AccessToken;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TwitterAuthHandler extends DefaultAuthHandler {

    private static final String SERVICE = "twitter";

    private ServiceBuilder serviceBuilder;

    private JedisPool jedisPool;

    private Twitter twitter;

    @Inject
    @Named("redis.db.requestTokens")
    private int database;

    @Inject
    public TwitterAuthHandler(
            Service service,
            ServiceBuilder serviceBuilder,
            JedisPoolFactory jedisPoolFactory,
            TwitterFactoryWrapper twitterFactory
    ) {
        super(service);
        this.serviceBuilder = serviceBuilder;
        jedisPool = jedisPoolFactory.build();
        twitter = twitterFactory.getInstance();
        twitter.setOAuthConsumer(service.getApikey(), service.getSecret());
    }

    public User auth(User user, String token) throws AuthHandlerException {
        throw new AuthHandlerException("Twitter OAuth MUST have a token and a verifier");
    }

    @Override
    public AuthenticatedUser auth(String verifier) throws AuthHandlerException {
        throw new AuthHandlerException("Twitter OAuth MUST have a token and a verifier");
    }

    @Override
    public AuthenticatedUser auth(String verifier, String finalRedirect)
            throws AuthHandlerException {
        throw new AuthHandlerException("Twitter OAuth MUST have a token and a verifier");
    }

    @Override
    public AuthenticatedUser auth(User user, String token, String verifier)
            throws AuthHandlerException {
        Verifier v = new Verifier(verifier);
        OAuthService twitterOAuth = serviceBuilder.build();

        Jedis jedis = jedisPool.getResource();
        String tokenSecret;

        try {
            jedis.select(database);
            tokenSecret = jedis.get(token);
        } finally {
            jedisPool.returnResource(jedis);
        }

        if (tokenSecret == null) {
            String message = "Request token [" + token + "] is either invalid or has expired.";
            throw new AuthHandlerException(message);
        }

        Token requestToken = new Token(token, tokenSecret);
        Token accessToken = twitterOAuth.getAccessToken(requestToken, v);

        String twitterId;
        try {
            twitter.setOAuthAccessToken(new AccessToken(accessToken.getToken(), accessToken.getSecret()));
            twitter4j.User twitterUser = twitter.verifyCredentials();
            twitterId = String.valueOf(twitterUser.getId());
        } catch (TwitterException twe) {
            // When Twitter service or network is unavailable,
            // or if supplied credentials are wrong.
            String message = (twe.getStatusCode() == 401)
                    ? "OAuth credentials are invalid."
                    : "Error connecting to Twitter.";
            throw new AuthHandlerException(message, twe);
        }

        user.addService(
                service.getName(),
                new OAuthAuth(accessToken.getToken(), accessToken.getSecret())
        );

        return new AuthenticatedUser(twitterId, user);
    }

    @Override
    public OAuthToken getToken() throws AuthHandlerException {
        return createOAuthServiceAndGetToken(service.getAtomicOAuthCallback().toString());
    }

    @Override
    public OAuthToken getToken(URL finalRedirectUrl) throws AuthHandlerException {
        String encodedFinalRedirect;
        try {
            encodedFinalRedirect = URLEncoder.encode(finalRedirectUrl.toString(), "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            throw new AuthHandlerException("UTF-8 encoding is not supported on this system.", uee);
        }

        String callback = service.getAtomicOAuthCallback() + "web/" + encodedFinalRedirect;

        return createOAuthServiceAndGetToken(callback);
    }

    public OAuthToken getToken(String username) throws AuthHandlerException {
        return createOAuthServiceAndGetToken(service.getOAuthCallback() + username);
    }

    @Override
    public OAuthToken getToken(String username, URL callback) throws AuthHandlerException {
        return createOAuthServiceAndGetToken(callback + username);
    }

    @Override
    public String getService() {
        return SERVICE;
    }

    @Override
    public List<Activity> grabActivities(String secret, String username, int limit)
            throws AuthHandlerException {
        throw new UnsupportedOperationException("nah, NIY.");
    }

    private OAuthToken createOAuthServiceAndGetToken(String callback) throws AuthHandlerException {
        OAuthService twitterOAuth = serviceBuilder
                .callback(callback)
                .build();
        Token token = twitterOAuth.getRequestToken();
        String redirectUrl = twitterOAuth.getAuthorizationUrl(token);

        try {
            return new OAuthToken(new URL(redirectUrl));
        } catch (MalformedURLException e) {
            throw new AuthHandlerException("The redirect URL is not well formed", e);
        }
    }
}