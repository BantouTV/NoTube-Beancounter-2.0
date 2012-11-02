package io.beancounter.usermanager.services.auth.twitter;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.beancounter.commons.helper.UriUtils;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import io.beancounter.commons.model.User;
import io.beancounter.commons.model.activity.*;
import io.beancounter.commons.model.activity.Tweet;
import org.apache.commons.codec.EncoderException;
import org.joda.time.DateTime;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import io.beancounter.commons.model.*;
import io.beancounter.commons.model.auth.AuthHandlerException;
import io.beancounter.commons.model.auth.AuthenticatedUser;
import io.beancounter.commons.model.auth.DefaultAuthHandler;
import io.beancounter.commons.model.OAuthToken;
import io.beancounter.commons.model.auth.OAuthAuth;
import redis.clients.jedis.exceptions.JedisConnectionException;
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

    protected static final Logger LOGGER = LoggerFactory.getLogger(TwitterAuthHandler.class);

    private static final String SERVICE = "twitter";

    private static final String CHANNEL = "register";

    // 2 hours
    private static final int EXPIRE_TIME = 7200;

    private ServiceBuilder serviceBuilder;

    private JedisPool jedisPool;

    private Twitter twitter;

    private final int database;

    private static final String TWITTER_BASE_URL = "http://twitter.com/";

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

        notify(twitterId, CHANNEL);

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
        notify(twitterId, CHANNEL);
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
    public List<Activity> grabActivities(OAuthAuth auth, String username, int limit)
            throws AuthHandlerException {
        twitter.setOAuthAccessToken(new AccessToken(auth.getSession(), auth.getSecret()));
        Paging paging = new Paging(1, limit);
        ResponseList<Status> statuses;
        try {
            statuses = twitter.getUserTimeline("", paging);
        } catch (TwitterException e) {
            final String errMsg = "error while getting tweets for user [" + username + "]";
            LOGGER.error(errMsg, e);
            throw new AuthHandlerException(errMsg, e);
        }
        return toActivities(statuses);
    }

    private List<Activity> toActivities(ResponseList<Status> statuses) {
        List<Activity> activities = new ArrayList<Activity>();
        for(Status status : statuses) {
            Activity activity;
            try {
                activity = toActivity(status);
            } catch (AuthHandlerException e) {
                // just log and skip
                LOGGER.error("error while converting this tweet {} to a beancounter activity", status, e);
                continue;
            }
            activities.add(activity);
        }
        return activities;
    }

    private Activity toActivity(Status status) throws AuthHandlerException {
        URL tweetUrl;
        String tweetUrlCandidate = TWITTER_BASE_URL + status.getUser().getName() + "/status/" + status.getId();
        try {
            tweetUrl = new URL(tweetUrlCandidate);
        } catch (MalformedURLException e) {
            throw new AuthHandlerException("error [" + tweetUrlCandidate + "] is ill-formed", e);
        }
        io.beancounter.commons.model.activity.Tweet tweet = new Tweet();
        tweet.setUrl(tweetUrl);
        tweet.setText(status.getText());
        for(HashtagEntity ht : status.getHashtagEntities()) {
            tweet.addHashTag(ht.getText());
        }
        for(URLEntity urlEntity : status.getURLEntities()) {
            tweet.addUrl(urlEntity.getExpandedURL());
        }
        Context context = new Context();
        context.setService("twitter");
        context.setUsername(status.getUser().getName());
        context.setDate(new DateTime(status.getCreatedAt().getTime()));
        Activity a = new Activity();
        a.setVerb(Verb.TWEET);
        a.setObject(tweet);
        a.setContext(context);
        return a;
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

    private void notify(String name, String channel) throws AuthHandlerException {
        Jedis jedis = getJedisResource();
        boolean isConnectionIssue = false;
        try {
            jedis.publish(channel, name);
        } catch (JedisConnectionException e) {
            final String errMsg = "Jedis Connection error while publishing filter [" + name + "]";
            LOGGER.error(errMsg, e);
            throw new AuthHandlerException(errMsg, e);
        } catch (Exception e) {
            final String errMsg = "Error while publishing filter [" + name + "]";
            LOGGER.error(errMsg, e);
            throw new AuthHandlerException(errMsg, e);
        } finally {
            if(isConnectionIssue) {
                jedisPool.returnBrokenResource(jedis);
            } else {
                jedisPool.returnResource(jedis);
            }
        }
    }

    private Jedis getJedisResource() throws AuthHandlerException {
        Jedis jedis;
        try {
            jedis = jedisPool.getResource();
        } catch (Exception e) {
            final String errMsg = "Error while getting a Jedis resource";
            LOGGER.error(errMsg, e);
            throw new AuthHandlerException(errMsg, e);
        }
        boolean isConnectionIssue = false;
        try {
            jedis.select(database);
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while selecting database [" + database + "]";
            LOGGER.error(errMsg, e);
            throw new AuthHandlerException(errMsg, e);
        } catch (Exception e) {
            jedisPool.returnResource(jedis);
            final String errMsg = "Error while selecting database [" + database + "]";
            LOGGER.error(errMsg, e);
            throw new AuthHandlerException(errMsg, e);
        } finally {
            if(isConnectionIssue) {
                jedisPool.returnBrokenResource(jedis);
            }
        }
        return jedis;
    }

}