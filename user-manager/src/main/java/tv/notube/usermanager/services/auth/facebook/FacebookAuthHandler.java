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

import java.net.MalformedURLException;
import java.net.URL;

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
        if(verifier == null) {
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
    public String getService() {
        return SERVICE;
    }

}