package tv.notube.usermanager.services.auth.twitter;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import tv.notube.commons.model.*;
import tv.notube.commons.model.auth.AuthHandlerException;
import tv.notube.commons.model.auth.AuthenticatedUser;
import tv.notube.commons.model.auth.DefaultAuthHandler;
import tv.notube.commons.model.OAuthToken;
import tv.notube.commons.model.auth.OAuthAuth;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TwitterAuthHandler extends DefaultAuthHandler {

    final private static String SERVICE = "http://twitter.com";

    public TwitterAuthHandler(Service service) {
        super(service);
    }

    public User auth(User user, String token) throws AuthHandlerException {
        throw new AuthHandlerException("Twitter OAuth MUST have a verifier");
    }

    public AuthenticatedUser auth(
            User user,
            String token,
            String verifier
    ) throws AuthHandlerException {
        if(verifier == null) {
            auth(user, token);
        }
        Verifier v = new Verifier(verifier);
        OAuthService twitterOAuth = new ServiceBuilder()
                           .provider(TwitterApi.class)
                           .apiKey(service.getApikey())
                           .apiSecret(service.getSecret())
                           .build();
        Token requestToken = new Token(token, service.getSecret());
        Token accessToken = twitterOAuth.getAccessToken(requestToken, v);
        user.addService(
                service.getName(),
                new OAuthAuth(accessToken.getToken(), accessToken.getSecret())
        );
        // TODO (really high) handle twitter usernames
        return new AuthenticatedUser("", user);
    }

    public OAuthToken getToken(String username) throws AuthHandlerException {
        OAuthService twitterOAuth = new ServiceBuilder()
                           .provider(TwitterApi.class)
                           .apiKey(service.getApikey())
                           .apiSecret(service.getSecret())
                           .callback(service.getOAuthCallback() + username)
                           .build();
        Token token = twitterOAuth.getRequestToken();
        String redirectUrl = twitterOAuth.getAuthorizationUrl(token);
        try {
            return new OAuthToken(new URL(redirectUrl));
        } catch (MalformedURLException e) {
            throw new AuthHandlerException(
                    "The redirect url is not well formed",
                    e
            );
        }
    }

    @Override
    public OAuthToken getToken(String username, URL callback) throws AuthHandlerException {
        throw new UnsupportedOperationException("nah, NIY.");
    }

    @Override
    public String getService() {
        return SERVICE;
    }

}