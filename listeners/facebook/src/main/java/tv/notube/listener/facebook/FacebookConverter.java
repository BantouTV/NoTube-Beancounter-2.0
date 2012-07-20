package tv.notube.listener.facebook;

import java.util.*;

import com.google.inject.Inject;
import com.restfb.*;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.types.Post;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tv.notube.commons.model.User;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.Verb;
import tv.notube.listener.facebook.converter.FacebookActivityConverter;
import tv.notube.listener.facebook.converter.FacebookActivityConverterException;
import tv.notube.listener.facebook.model.FacebookChange;
import tv.notube.listener.facebook.model.FacebookData;
import tv.notube.listener.facebook.model.FacebookNotification;
import tv.notube.resolver.Resolver;
import tv.notube.resolver.ResolverException;
import tv.notube.usermanager.UserManager;
import tv.notube.usermanager.UserManagerException;

public class FacebookConverter implements ActivityConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookConverter.class);

    private final static String SERVICE = "facebook";

    @Inject
    private Resolver resolver;

    @Inject
    private UserManager userManager;

    @Inject
    private FacebookActivityConverter converter;

    public List<Activity> getActivities(FacebookNotification notification) {
        List<Activity> activities = new ArrayList<Activity>();
        Set<String> excludedUsers = new HashSet<String>();
        for (FacebookChange change : notification.getEntry()) {
            String userId = change.getUid();
            if (excludedUsers.contains(userId)) {
                // exclude users with expired token
                continue;
            }
            // getting the user token
            String token = getAccessToken(userId);
            LOGGER.debug("token: {}", token);
            FacebookClient client = new DefaultFacebookClient(token);
            for (String field : change.getChangedFields()) {
                LOGGER.debug("looking for field {}", field);
                Collection<java.lang.Object> fetchedObjs;
                Class<?> facebookClass = getFacebookClass(field);
                try {
                    fetchedObjs = fetch(facebookClass, client, field);
                } catch (FacebookOAuthException e) {
                    String username = getUsername(userId);
                    LOGGER.debug("token expired for user:{}", username);
                    User user = getUser(username);
                    try {
                        userManager.voidOAuthToken(user, SERVICE);
                    } catch (UserManagerException e1) {
                        final String errMgs = "error while voiding the OAuth token for user [" + user.getUsername() + "] on service [" + SERVICE + "]";
                        LOGGER.error(errMgs, e1);
                        throw new RuntimeException(errMgs, e1);
                    }
                    excludedUsers.add(userId);
                    continue;
                }
                for (java.lang.Object fetchedObj : fetchedObjs) {
                    Verb verb = fromFieldToVerb(field);
                    FacebookActivityConverter.Result result;
                    try {
                        result = converter.convert(fetchedObj, fromFieldToVerb(field));
                    } catch (FacebookActivityConverterException e) {
                        final String errMgs = "Error while converting Facebook object " +
                                "[" + fetchedObj.toString() + "] for field [" + field + "]";
                        LOGGER.error(errMgs, e);
                        throw new RuntimeException(errMgs, e);
                    }
                    Activity activity = toActivity(result, verb);
                    activities.add(activity);
                }
            }
        }
        return activities;
    }

    private Class<?> getFacebookClass(String field) {
        // TODO (high) (put this in the properties and make it real)
        if(!field.equals("feed")) {
            return FacebookData.class;
        }
        return Post.class;
    }

    private Activity toActivity(FacebookActivityConverter.Result result, Verb verb) {
        Activity activity = new Activity();
        activity.setVerb(verb);
        activity.setObject(result.getObject());
        activity.setContext(result.getContext());
        return activity;
    }

    private Verb fromFieldToVerb(String field) {
        // TODO (high) (put this in the properties and make it real)
        if(!field.equals("feed")) {
            return Verb.LIKE;
        }
        return Verb.SHARE;
    }

    private <T> Collection<T> fetch(
            Class<? extends T> clazz,
            FacebookClient client,
            String field
    ) throws FacebookOAuthException {
        Connection<T> connection = (Connection<T>) client.fetchConnection(
                "me/" + field,
                clazz,
                Parameter.with("limit", 1)
        );
        Collection<T> result = new ArrayList<T>();
        for (T t : connection.getData()) {
            result.add(t);
        }
        return result;
    }

    private String getAccessToken(String identifier) {
        String username;
        try {
            username = resolver.resolveUsername(identifier, SERVICE);
        } catch (ResolverException e) {
            final String errMsg = "Error while resolving username [" + identifier + "] on facebook";
            LOGGER.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
        User userObj;
        try {
            userObj = userManager.getUser(username);
        } catch (UserManagerException e) {
            final String errMsg = "Error while getting user with username [" + username + "]";
            LOGGER.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
        return userObj.getServices().get(SERVICE).getSession();
    }

    private String getUsername(String identifier) {
        String username;
        try {
            username = resolver.resolveUsername(identifier, SERVICE);
        } catch (ResolverException e) {
            final String errMsg = "Error while resolving username [" + identifier + "] on facebook";
            LOGGER.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
        return username;
    }

    private User getUser(String username) {
        try {
            return userManager.getUser(username);
        } catch (UserManagerException e) {
            final String errMsg = "Error while getting user [" + username + "]";
            LOGGER.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
    }
}