package io.beancounter.usermanager.services.auth.twitter;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.beancounter.commons.helper.UriUtils;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import io.beancounter.commons.model.User;
import org.apache.commons.codec.EncoderException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import io.beancounter.commons.model.*;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.auth.AuthHandlerException;
import io.beancounter.commons.model.auth.AuthenticatedUser;
import io.beancounter.commons.model.auth.DefaultAuthHandler;
import io.beancounter.commons.model.OAuthToken;
import io.beancounter.commons.model.auth.OAuthAuth;
import twitter4j.*;
import twitter4j.auth.AccessToken;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TwitterAuthHandler extends DefaultAuthHandler {

    private static final String SERVICE = "twitter";

    // 2 hours
    private static final int EXPIRE_TIME = 7200;

    private ServiceBuilder serviceBuilder;

    private JedisPool jedisPool;

    private Twitter twitter;

    private final int database;

    @Inject
    public TwitterAuthHandler(
            @Named("service.twitter") Service service,
            ServiceBuilder serviceBuilder,
            JedisPoolFactory jedisPoolFactory,
            TwitterFactoryWrapper twitterFactory,
            @Named("redis.db.requestTokens") int database
    ) {
        super(service);
        this.serviceBuilder = serviceBuilder;
        this.serviceBuilder
                .provider(TwitterApi.Authenticate.class)
                .apiKey(service.getApikey())
                .apiSecret(service.getSecret());
        this.database = database;
        jedisPool = jedisPoolFactory.build();
        twitter = twitterFactory.getInstance();
        twitter.setOAuthConsumer(service.getApikey(), service.getSecret());
    }

    @Override
    public User auth(User user, String token) throws AuthHandlerException {
        throw new AuthHandlerException("Twitter OAuth MUST have a token and a verifier");
    }

    @Override
    public AuthenticatedUser auth(String verifier) throws AuthHandlerException {
        throw new AuthHandlerException("Twitter OAuth MUST have a token and a verifier");
    }

    @Override
    public AuthenticatedUser authWithRedirect(String verifier, String finalRedirect)
            throws AuthHandlerException {
        throw new AuthHandlerException("Twitter OAuth MUST have a token and a verifier");
    }

    @Override
    public AuthenticatedUser auth(User user, String token, String verifier)
            throws AuthHandlerException {
        Token accessToken = getTwitterAccessToken(token, verifier);
        twitter4j.User twitterUser = getTwitterUser(accessToken);

        String twitterId = String.valueOf(twitterUser.getId());
        user.addService(
                service.getName(),
                new OAuthAuth(accessToken.getToken(), accessToken.getSecret())
        );

        return new AuthenticatedUser(twitterId, user);
    }

    @Override
    public AuthenticatedUser auth(String token, String verifier) throws AuthHandlerException {
        Token accessToken = getTwitterAccessToken(token, verifier);
        twitter4j.User twitterUser = getTwitterUser(accessToken);

        // Users created in this way will have beancounter username equals
        // to their Twitter id.
        // TODO (high) implement a retry policy to be sure it's unique
        String twitterId = String.valueOf(twitterUser.getId());
        User user = new User();
        user.setUsername(twitterId);
        user.addMetadata("twitter.user.name", twitterUser.getName());
        user.addMetadata("twitter.user.screenName", twitterUser.getScreenName());
        user.addMetadata("twitter.user.description", twitterUser.getDescription());
        user.addMetadata("twitter.user.imageUrl", twitterUser.getProfileImageURL().toString());

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
            encodedFinalRedirect = UriUtils.encodeBase64(finalRedirectUrl.toString());
        } catch (UnsupportedEncodingException uee) {
            throw new AuthHandlerException("UTF-8 encoding is not supported on this system.", uee);
        } catch (EncoderException eex) {
            throw new AuthHandlerException(
                    "Error while encoding final redirect URL [" + finalRedirectUrl + "].",
                    eex
            );
        }

        String callback = service.getAtomicOAuthCallback() + "web/" + encodedFinalRedirect;

        return createOAuthServiceAndGetToken(callback);
    }

    @Override
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
        Jedis jedis = jedisPool.getResource();

        try {
            jedis.select(database);
            jedis.set(token.getToken(), token.getSecret());
            jedis.expire(token.getToken(), EXPIRE_TIME);
        } finally {
            jedisPool.returnResource(jedis);
        }

        try {
            return new OAuthToken(new URL(redirectUrl));
        } catch (MalformedURLException e) {
            throw new AuthHandlerException("The redirect URL is not well formed.", e);
        }
    }

    private Token getTwitterAccessToken(String token, String verifier) throws AuthHandlerException {
        Verifier v = new Verifier(verifier);
        OAuthService twitterOAuth = serviceBuilder.build();

        Jedis jedis = jedisPool.getResource();
        String tokenSecret;

        try {
            jedis.select(database);
            tokenSecret = jedis.get(token);
            jedis.del(token);
        } finally {
            jedisPool.returnResource(jedis);
        }

        if (tokenSecret == null) {
            String message = "Request token [" + token + "] is either invalid or has expired.";
            throw new AuthHandlerException(message);
        }

        Token requestToken = new Token(token, tokenSecret);
        return twitterOAuth.getAccessToken(requestToken, v);
    }

    private twitter4j.User getTwitterUser(Token accessToken) throws AuthHandlerException {
        twitter4j.User twitterUser;

        try {
            twitter.setOAuthAccessToken(new AccessToken(accessToken.getToken(), accessToken.getSecret()));
            twitterUser = twitter.verifyCredentials();
        } catch (TwitterException twe) {
            // When Twitter service or network is unavailable,
            // or if supplied credentials are wrong.
            String message = (twe.getStatusCode() == 401)
                    ? "OAuth credentials are invalid."
                    : "Error connecting to Twitter.";
            throw new AuthHandlerException(message, twe);
        }

        return twitterUser;
    }
}