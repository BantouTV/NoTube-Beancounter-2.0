package io.beancounter.listener.facebook;

import java.util.*;

import com.google.inject.Inject;
import com.restfb.*;
import com.restfb.exception.FacebookOAuthException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.beancounter.commons.model.User;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.Verb;
import io.beancounter.listener.commons.ActivityConverter;
import io.beancounter.listener.commons.ActivityConverterException;
import io.beancounter.listener.facebook.core.FacebookUtils;
import io.beancounter.listener.facebook.core.converter.FacebookActivityConverter;
import io.beancounter.listener.facebook.core.converter.FacebookActivityConverterException;
import io.beancounter.listener.facebook.core.converter.UnconvertableFacebookActivityException;
import io.beancounter.listener.facebook.core.model.FacebookChange;
import io.beancounter.listener.facebook.core.model.FacebookNotification;
import io.beancounter.resolver.Resolver;
import io.beancounter.resolver.ResolverException;
import io.beancounter.usermanager.UserManager;
import io.beancounter.usermanager.UserManagerException;

/**
 * In-memory implementation of {@link ActivityConverter}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public final class FacebookNotificationConverter implements ActivityConverter<FacebookNotification> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookNotificationConverter.class);

    private final static String SERVICE = "facebook";

    @Inject
    private Resolver resolver;

    @Inject
    private UserManager userManager;

    @Inject
    private FacebookActivityConverter converter;

    private Activity toActivity(FacebookActivityConverter.Result result, Verb verb) {
        Activity activity = new Activity();
        activity.setVerb(verb);
        activity.setObject(result.getObject());
        activity.setContext(result.getContext());
        return activity;
    }

    private String getAccessToken(String identifier) throws ActivityConverterException {
        String username;
        try {
            username = resolver.resolveUsername(identifier, SERVICE);
        } catch (ResolverException e) {
            final String errMsg = "Error while resolving username [" + identifier + "] on facebook";
            LOGGER.error(errMsg, e);
            throw new ActivityConverterException(errMsg, e);
        }
        User userObj;
        try {
            userObj = userManager.getUser(username);
        } catch (UserManagerException e) {
            final String errMsg = "Error while getting user with username [" + username + "]";
            LOGGER.error(errMsg, e);
            throw new ActivityConverterException(errMsg, e);
        }
        return userObj.getServices().get(SERVICE).getSession();
    }

    private String getUsername(String identifier) throws ActivityConverterException {
        String username;
        try {
            username = resolver.resolveUsername(identifier, SERVICE);
        } catch (ResolverException e) {
            final String errMsg = "Error while resolving username [" + identifier + "] on facebook";
            LOGGER.error(errMsg, e);
            throw new ActivityConverterException(errMsg, e);
        }
        return username;
    }

    private User getUser(String username) throws ActivityConverterException {
        try {
            return userManager.getUser(username);
        } catch (UserManagerException e) {
            final String errMsg = "Error while getting user [" + username + "]";
            LOGGER.error(errMsg, e);
            throw new ActivityConverterException(errMsg, e);
        }
    }

    @Override
    public List<Activity> getActivities(FacebookNotification notification)
            throws ActivityConverterException {
        List<Activity> activities = new ArrayList<Activity>();
        for (FacebookChange change : notification.getEntry()) {
            String userId = change.getUid();
            // getting the user token
            String token = getAccessToken(userId);
            LOGGER.debug("token: {}", token);
            FacebookClient client = new DefaultFacebookClient(token);
            for (String field : change.getChangedFields()) {
                LOGGER.debug("looking for field {}", field);
                Collection<java.lang.Object> fetchedObjs;
                Class<?> facebookClass = FacebookUtils.getFacebookClass(field);
                try {
                    LOGGER.debug("fetching {} {}", facebookClass.getName(), field);
                    fetchedObjs = FacebookUtils.fetch(facebookClass, client, field, 1);
                } catch (FacebookOAuthException e) {
                    String username = getUsername(userId);
                    LOGGER.debug("token expired for user:{}", username);
                    User user = getUser(username);
                    try {
                        userManager.voidOAuthToken(user, SERVICE);
                    } catch (UserManagerException e1) {
                        final String errMgs = "error while voiding the OAuth token for user [" + user.getUsername() + "] on service [" + SERVICE + "]";
                        LOGGER.error(errMgs, e1);
                        throw new ActivityConverterException(errMgs, e1);
                    }
                    continue;
                }
                for (java.lang.Object fetchedObj : fetchedObjs) {
                    Verb verb = FacebookUtils.fromFieldToVerb(field);
                    FacebookActivityConverter.Result result;
                    try {
                        LOGGER.debug("converting {} to {}", fetchedObj, verb);
                        result = converter.convert(fetchedObj, verb, userId);
                    } catch (UnconvertableFacebookActivityException e) {
                        LOGGER.error("skipping an unconvertable object");
                        continue;
                    } catch (FacebookActivityConverterException e) {
                        final String errMgs = "Error while converting Facebook object " +
                                "[" + fetchedObj.toString() + "] for field [" + field + "]";
                        LOGGER.error(errMgs, e);
                        throw new ActivityConverterException(errMgs, e);
                    }
                    Activity activity = toActivity(result, verb);
                    activities.add(activity);
                }
            }
        }
        LOGGER.debug("returning activities {}", activities);
        return activities;
    }
}