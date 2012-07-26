package tv.notube.usermanager.services.auth.facebook;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import tv.notube.commons.model.*;
import tv.notube.commons.model.User;
import tv.notube.commons.model.auth.AuthenticatedUser;
import tv.notube.commons.model.auth.DefaultAuthHandler;
import tv.notube.commons.model.OAuthToken;
import tv.notube.commons.model.auth.AuthHandlerException;
import tv.notube.commons.model.auth.OAuthAuth;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FacebookAuthHandler extends DefaultAuthHandler {

    final static String SERVICE = "facebook";

    public FacebookAuthHandler(Service service) {
        super(service);
    }

    public User auth(User user, String token) throws AuthHandlerException {
        throw new AuthHandlerException("Facebook OAuth MUST have a verifier");
    }

    public AuthenticatedUser auth(
            User user,
            String token,
            String verifier
    ) throws AuthHandlerException {
        if (verifier == null) {
            auth(user, token);
        }
        Verifier v = new Verifier(verifier);
        OAuthService facebookOAuth = new ServiceBuilder()
                .provider(FacebookApi.class)
                .apiKey(service.getApikey())
                .apiSecret(service.getSecret())
                .scope("read_stream,user_likes,user_location,user_interests,user_activities")
                .callback(service.getOAuthCallback() + user.getUsername())
                .build();
        Token requestToken = null;
        Token accessToken = facebookOAuth.getAccessToken(requestToken, v);
        user.addService(
                service.getName(),
                new OAuthAuth(accessToken.getToken(), accessToken.getSecret())
        );
        String facebookUserId = getUserId(accessToken.getToken());
        return new AuthenticatedUser(facebookUserId, user);
    }

    private String getUserId(String token) {
        FacebookClient client = new DefaultFacebookClient(token);
        com.restfb.types.User user = client.fetchObject("me", com.restfb.types.User.class);
        return user.getId();
    }

    public OAuthToken getToken(String username) throws AuthHandlerException {
        OAuthService facebookOAuth = new ServiceBuilder()
                .provider(FacebookApi.class)
                .apiKey(service.getApikey())
                .apiSecret(service.getSecret())
                .scope("read_stream,user_likes,user_location,user_interests,user_activities")
                .callback(service.getOAuthCallback() + username)
                .build();
        Token token = null;
        String redirectUrl = facebookOAuth.getAuthorizationUrl(token);
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
    public OAuthToken getToken() throws AuthHandlerException {
        // TODO (high) what if a user is called atomic? use a different callback property
        OAuthService facebookOAuth = new ServiceBuilder()
                .provider(FacebookApi.class)
                .apiKey(service.getApikey())
                .apiSecret(service.getSecret())
                .scope("read_stream,user_likes,user_location,user_interests,user_activities")
                .callback(service.getOAuthCallback() + "atomic")
                .build();
        Token token = null;
        String redirectUrl = facebookOAuth.getAuthorizationUrl(token);
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
        OAuthService facebookOAuth;
        facebookOAuth = new ServiceBuilder()
                .provider(FacebookApi.class)
                .apiKey(service.getApikey())
                .apiSecret(service.getSecret())
                .scope("read_stream,user_likes,user_location,user_interests,user_activities")
                .callback(callback + username)
                .build();
        Token token = null;
        String redirectUrl = facebookOAuth.getAuthorizationUrl(token);
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
    public AuthenticatedUser auth(String verifier) throws AuthHandlerException {
        if (verifier == null) {
            auth(null, null);
        }
        // TODO (high) what if a user is called atomic? use a different callback property
        Verifier v = new Verifier(verifier);
        OAuthService facebookOAuth = new ServiceBuilder()
                .provider(FacebookApi.class)
                .apiKey(service.getApikey())
                .apiSecret(service.getSecret())
                .scope("read_stream,user_likes,user_location,user_interests,user_activities")
                .callback(service.getOAuthCallback() + "atomic")
                .build();
        Token requestToken = null;
        Token accessToken = facebookOAuth.getAccessToken(requestToken, v);
        String facebookUserId = getUserId(accessToken.getToken());
        User user = createNewUser(facebookUserId);
        user.addService(
                service.getName(),
                new OAuthAuth(accessToken.getToken(), accessToken.getSecret())
        );
        return new AuthenticatedUser(facebookUserId, user);
    }

    private User createNewUser(String facebookUserId) {
        // users created in this way will have beanocunter username equals
        // to the facebook one.
        // TODO (high) implement a retry policy to be sure it's unique
        User user = new User();
        user.setUsername(String.valueOf(facebookUserId));
        return user;
    }

    @Override
    public String getService() {
        return SERVICE;
    }

}