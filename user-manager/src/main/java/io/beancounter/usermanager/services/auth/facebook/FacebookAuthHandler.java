package io.beancounter.usermanager.services.auth.facebook;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.Post;
import io.beancounter.commons.helper.UriUtils;
import org.apache.commons.codec.EncoderException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import io.beancounter.commons.helper.reflection.ReflectionHelper;
import io.beancounter.commons.helper.reflection.ReflectionHelperException;
import io.beancounter.commons.model.*;
import io.beancounter.commons.model.User;
import io.beancounter.commons.model.activity.*;
import io.beancounter.commons.model.activity.Object;
import io.beancounter.commons.model.auth.AuthenticatedUser;
import io.beancounter.commons.model.auth.DefaultAuthHandler;
import io.beancounter.commons.model.OAuthToken;
import io.beancounter.commons.model.auth.AuthHandlerException;
import io.beancounter.commons.model.auth.OAuthAuth;
import io.beancounter.listener.facebook.core.FacebookUtils;
import io.beancounter.listener.facebook.core.converter.custom.ConverterException;
import io.beancounter.listener.facebook.core.converter.custom.FacebookLikeConverter;
import io.beancounter.listener.facebook.core.converter.custom.FacebookShareConverter;
import io.beancounter.listener.facebook.core.model.FacebookData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FacebookAuthHandler extends DefaultAuthHandler {

    protected static final Logger LOGGER = LoggerFactory.getLogger(FacebookAuthHandler.class);

    final static String SERVICE = "facebook";

    @Inject
    public FacebookAuthHandler(@Named("service.facebook") Service service) {
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
                .scope("read_stream,user_likes,user_location,user_interests,user_activities,publish_actions")
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
                .scope("read_stream,user_likes,user_location,user_interests,user_activities,publish_actions")
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
                .scope("read_stream,user_likes,user_location,user_interests,user_activities,publish_actions")
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

    public OAuthToken getToken(URL finalRedirectUrl) throws AuthHandlerException {
        String encodedRedirect;
        try {
            encodedRedirect = UriUtils.encodeBase64(finalRedirectUrl.toString());
        } catch (UnsupportedEncodingException uee) {
            throw new AuthHandlerException("UTF-8 encoding is not supported on this system.", uee);
        } catch (EncoderException eex) {
            throw new AuthHandlerException(
                    "Error while encoding final redirect URL [" + finalRedirectUrl + "].",
                    eex
            );
        }

        OAuthService facebookOAuth = new ServiceBuilder()
                .provider(FacebookApi.class)
                .apiKey(service.getApikey())
                .apiSecret(service.getSecret())
                .scope("read_stream,user_likes,user_location,user_interests,user_activities,publish_actions")
                .callback(service.getAtomicOAuthCallback().toString() + "web/" + encodedRedirect)
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
                .scope("read_stream,user_likes,user_location,user_interests,user_activities,publish_actions")
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
            auth((User) null, null);
        }
        Verifier v = new Verifier(verifier);
        OAuthService facebookOAuth = new ServiceBuilder()
                .provider(FacebookApi.class)
                .apiKey(service.getApikey())
                .apiSecret(service.getSecret())
                .scope("read_stream,user_likes,user_location,user_interests,user_activities,publish_actions")
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
    public AuthenticatedUser authWithRedirect(String verifier, String finalRedirect)
            throws AuthHandlerException {
        if (verifier == null) {
            auth((User) null, null);
        }

        String encodedRedirect;
        try {
            encodedRedirect = UriUtils.encodeBase64(finalRedirect);
        } catch (UnsupportedEncodingException uee) {
            throw new AuthHandlerException("UTF-8 encoding is not supported on this system.", uee);
        } catch (EncoderException eex) {
            throw new AuthHandlerException(
                    "Error while encoding final redirect URL [" + finalRedirect + "].",
                    eex
            );
        }

        Verifier v = new Verifier(verifier);
        OAuthService facebookOAuth = new ServiceBuilder()
                .provider(FacebookApi.class)
                .apiKey(service.getApikey())
                .apiSecret(service.getSecret())
                .scope("read_stream,user_likes,user_location,user_interests,user_activities,publish_actions")
                .callback(service.getAtomicOAuthCallback().toString() + "web/" + encodedRedirect)
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
    public AuthenticatedUser auth(String token, String verifier) throws AuthHandlerException {
        return auth(verifier);
    }

    @Override
    public List<Activity> grabActivities(OAuthAuth auth, String identifier, int limit)
            throws AuthHandlerException {
        FacebookClient client = new DefaultFacebookClient(auth.getSession());
        // grab shares
        Collection<Post> posts = FacebookUtils.fetch(Post.class, client, "feed", limit);
        FacebookShareConverter shareConverter = new FacebookShareConverter();
        List<Activity> result = new ArrayList<Activity>();
        for (Post post : posts) {
            io.beancounter.commons.model.activity.Object object;
            Context context;
            try {
                object = shareConverter.convert(post, true);
                context = shareConverter.getContext(post, identifier);
            } catch (ConverterException e) {
                // just log and skip
                LOGGER.error("error while converting Facebook POST from user {}", identifier, e);
                continue;
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
            io.beancounter.commons.model.activity.Object object;
            Context context;
            try {
                object = likeConverter.convert(like, true);
                context = likeConverter.getContext(like, identifier);
            } catch (ConverterException e) {
                // just log and skip
                LOGGER.error("error while converting Facebook LIKE from user {}", identifier, e);
                continue;
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
        // users created in this way will have beancounter username equals
        // to the facebook one.
        // TODO (high) implement a retry policy to be sure it's unique
        String candidateBCUsername = String.valueOf(data.get("facebook.user.id"));
        User user = new User();
        user.setUsername(candidateBCUsername);
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