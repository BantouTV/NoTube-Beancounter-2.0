package tv.notube.usermanager.services.auth.facebook;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.Post;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import tv.notube.commons.helper.reflection.ReflectionHelper;
import tv.notube.commons.helper.reflection.ReflectionHelperException;
import tv.notube.commons.model.*;
import tv.notube.commons.model.User;
import tv.notube.commons.model.activity.*;
import tv.notube.commons.model.activity.Object;
import tv.notube.commons.model.auth.AuthenticatedUser;
import tv.notube.commons.model.auth.DefaultAuthHandler;
import tv.notube.commons.model.OAuthToken;
import tv.notube.commons.model.auth.AuthHandlerException;
import tv.notube.commons.model.auth.OAuthAuth;
import tv.notube.listener.facebook.core.FacebookUtils;
import tv.notube.listener.facebook.core.converter.custom.ConverterException;
import tv.notube.listener.facebook.core.converter.custom.FacebookLikeConverter;
import tv.notube.listener.facebook.core.converter.custom.FacebookShareConverter;
import tv.notube.listener.facebook.core.model.*;
import tv.notube.listener.facebook.core.model.FacebookData;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

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
        OAuthService facebookOAuth = new ServiceBuilder()
                .provider(FacebookApi.class)
                .apiKey(service.getApikey())
                .apiSecret(service.getSecret())
                .scope("read_stream,user_likes,user_location,user_interests,user_activities")
                .callback(service.getAtomicOAuthCallback().toString())
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
        Verifier v = new Verifier(verifier);
        OAuthService facebookOAuth = new ServiceBuilder()
                .provider(FacebookApi.class)
                .apiKey(service.getApikey())
                .apiSecret(service.getSecret())
                .scope("read_stream,user_likes,user_location,user_interests,user_activities")
                .callback(service.getAtomicOAuthCallback().toString())
                .build();
        Token requestToken = null;
        Token accessToken = facebookOAuth.getAccessToken(requestToken, v);
        Map<String, String> data;
        try {
            data = getUserData(accessToken.getToken());
        } catch (Exception e) {
            final String errMsg = "Error while getting data for anonymous user";
            throw new AuthHandlerException(errMsg, e);
        }
        User user = createNewUser(data);
        user.addService(
                service.getName(),
                new OAuthAuth(accessToken.getToken(), accessToken.getSecret())
        );
        return new AuthenticatedUser(data.get("facebook.user.id"), user);
    }

    @Override
    public List<Activity> grabActivities(String token, String identifier, int limit)
            throws AuthHandlerException {
        FacebookClient client = new DefaultFacebookClient(token);
        // grab shares
        Collection<Post> posts = FacebookUtils.fetch(Post.class, client, "feed", limit);
        FacebookShareConverter shareConverter = new FacebookShareConverter();
        List<Activity> result = new ArrayList<Activity>();
        for (Post post : posts) {
            tv.notube.commons.model.activity.Object object;
            Context context;
            try {
                object = shareConverter.convert(post, true);
                context = shareConverter.getContext(post, identifier);
            } catch (ConverterException e) {
                // just skip
                continue;
                // TODO (med) should log
            }
            Activity activity = toActivity(object, Verb.SHARE);
            activity.setContext(context);
            result.add(activity);
        }
        // grab likes
        Collection<FacebookData> likes = FacebookUtils.fetch(
                FacebookData.class,
                client,
                "likes",
                limit
        );
        FacebookLikeConverter likeConverter = new FacebookLikeConverter();
        for (FacebookData like : likes) {
            tv.notube.commons.model.activity.Object object;
            Context context;
            try {
                object = likeConverter.convert(like, true);
                context = likeConverter.getContext(like, identifier);
            } catch (ConverterException e) {
                // just skip
                continue;
                // TODO (med) should log
            }
            Activity activity = toActivity(object, Verb.LIKE);
            activity.setContext(context);
            result.add(activity);
        }
        return result;
    }

    private Activity toActivity(Object object, Verb verb) {
        Activity activity = new Activity();
        activity.setVerb(verb);
        activity.setObject(object);
        return activity;
    }

    private Map<String, String> getUserData(String token) throws AuthHandlerException {
        FacebookClient client = new DefaultFacebookClient(token);
        CustomFacebookUser user = client.fetchObject(
                "me",
                CustomFacebookUser.class,
                Parameter.with("fields", "id, first_name, last_name, picture, gender")
        );
        ReflectionHelper.Access[] fields;
        try {
            // let's grab only primitives and String
            fields = ReflectionHelper.getGetters(CustomFacebookUser.class, true, String.class);
        } catch (ReflectionHelperException e) {
            final String errMsg = "Error while getting getters from [" + CustomFacebookUser.class.getName() + "]";
            throw new AuthHandlerException(errMsg, e);
        }
        Map<String, String> data = new HashMap<String, String>();
        for (ReflectionHelper.Access field : fields) {
            String key = "facebook.user." + field.getField();
            String value;
            try {
                value = ReflectionHelper.invokeAndCast(field.getMethod(), user, String.class);
            } catch (ReflectionHelperException e) {
                final String errMsg = "Error while invoking [" + field.getMethod() + "] from [" + CustomFacebookUser.class.getName() + "]";
                throw new AuthHandlerException(errMsg, e);
            }
            data.put(key, value);
        }
        return data;
    }

    private User createNewUser(Map<String, String> data) {
        // users created in this way will have beanocunter username equals
        // to the facebook one.
        // TODO (high) implement a retry policy to be sure it's unique
        String candindateBCUsername = String.valueOf(data.get("facebook.user.id"));
        User user = new User();
        user.setUsername(candindateBCUsername);
        for (String k : data.keySet()) {
            user.addMetadata(k, data.get(k));
        }
        return user;
    }

    @Override
    public String getService() {
        return SERVICE;
    }

}