package tv.notube.listener.facebook;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.types.Post;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tv.notube.commons.model.User;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.Context;
import tv.notube.commons.model.activity.Object;
import tv.notube.commons.model.activity.Verb;
import tv.notube.listener.facebook.model.FacebookChange;
import tv.notube.listener.facebook.model.FacebookData;
import tv.notube.listener.facebook.model.FacebookNotification;
import tv.notube.resolver.Resolver;
import tv.notube.resolver.ResolverException;
import tv.notube.usermanager.UserManager;
import tv.notube.usermanager.UserManagerException;

public class FacebookActivityConverter implements ActivityConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookActivityConverter.class);

    private final static String SERVICE = "facebook";

    @Inject
    private Resolver resolver;

    @Inject
    private UserManager userManager;


    private long getOldTimeStamp() {
        return 1;
    }

    public List<Activity> getActivities(FacebookNotification notification) {
        List<Activity> activities = new ArrayList<Activity>();
        Set<String> excludedUsers = new HashSet<String>();
        for (FacebookChange change : notification.getEntry()) {
            String userId = change.getUid();
            if (excludedUsers.contains(userId)) {
                continue;
            }
            // getting the user token
            String token = getAccessToken(userId);
            LOGGER.debug("token: {}", token);
            FacebookClient client = new DefaultFacebookClient(token);
            for (String field : change.getChangedFields()) {
                LOGGER.debug("looking for field {}", field);
                try {
                    if (field.compareTo("feed") != 0) {
                        activities.addAll(fetchLikes(client, field, userId));
                    } else {
                        activities.addAll(fetchFeeds(client, field, userId));
                    }
                } catch (FacebookOAuthException e) {
                    String username = getUsername(userId);
                    LOGGER.debug("token expired for user:{}", username);
                    User user = getUser(username);
                    try {
                        userManager.voidOAuthToken(user, SERVICE);
                    } catch (UserManagerException e1) {
                        final String errMgs = "error while voiding the OAuth token for user [" + user
                                .getUsername() + "] on service [" + SERVICE + "]";
                        LOGGER.error(errMgs, e1);
                        throw new RuntimeException(errMgs, e1);
                    }
                    excludedUsers.add(userId);
                    continue;
                }
            }
        }
        return activities;
    }

    private List<Activity> fetchLikes(FacebookClient client, String field, String userId)
            throws FacebookOAuthException {
        // TODO (low) limit should be configurable
        Connection<FacebookData> entities =
                client.fetchConnection(
                        "me/" + field,
                        FacebookData.class,
                        Parameter.with("limit", 1)
                );
        List<FacebookData> filteredLikes = filterLikes(entities);
        LOGGER.debug("RECEIVED ENTITIES (already filtered): " + filteredLikes.toString());
        return convertLikesToActivities(userId, filteredLikes);
    }


    private List<FacebookData> filterLikes(Connection<FacebookData> entities) {
        List<FacebookData> result = new ArrayList<FacebookData>();
        for (FacebookData entity : entities.getData()) {
            DateTime date = new DateTime(entity.getCreatedTime());
            // TODO (high) persist and retrieve correctly
            if (date.isAfter(getOldTimeStamp())) {
                result.add(entity);
            }
        }
        return result;
    }

    private List<Activity> convertLikesToActivities(String userId, List<FacebookData> likes) {
        List<Activity> activities = new ArrayList<Activity>();
        for (FacebookData like : likes) {
            try {
                Activity activity = new Activity();
                // TODO (med)
                activity.setVerb(Verb.LIKE);
                tv.notube.commons.model.activity.Object object
                        = new tv.notube.commons.model.activity.Object();
                object.setName(like.getName());
                object.setDescription(like.getCategory());
                object.setUrl(new URL("http://www.facebook.com/" + like.getId()));
                activity.setObject(object);
                Context context = new Context();
                context.setUsername(userId);
                context.setDate(new DateTime(like.getCreatedTime()));
                context.setService("facebook");
                activity.setContext(context);
                activities.add(activity);
            } catch (MalformedURLException e) {
                LOGGER.error("the url is malformed");
            }
        }
        return activities;
    }

    private List<Activity> fetchFeeds(FacebookClient client, String field, String userId)
            throws FacebookOAuthException {
        // TODO (low) limit should be configurable
        Connection<Post> feeds =
                client.fetchConnection(
                        "me/" + field,
                        Post.class,
                        Parameter.with("limit", 1)
                );
        List<Post> filteredFeeds = filterFeeds(feeds);
        LOGGER.debug("RECEIVED ENTITIES (already filtered): " + filteredFeeds.toString());
        return convertFeedsToActivities(userId, filteredFeeds);
    }

    private List<Post> filterFeeds(Connection<Post> feeds) {
        List<Post> result = new ArrayList<Post>();
        for (Post feed : feeds.getData()) {
            DateTime date = new DateTime(feed.getCreatedTime());
            // TODO (high) persist and retrieve correctly
            if (date.isAfter(getOldTimeStamp())) {
                result.add(feed);
            }
        }
        return result;
    }

    private List<Activity> convertFeedsToActivities(String userId, List<Post> feeds) {
        List<Activity> activities = new ArrayList<Activity>();
        for (Post feed : feeds) {
            if (feed.getFrom().getId().compareTo(userId) == 0 && feed.getType().compareTo("link") == 0) {
                try {
                    LOGGER.debug("CONVERTING FEED: " + feed.toString());
                    Activity activity = new Activity();
                    activity.setVerb(Verb.LIKE);
                    tv.notube.commons.model.activity.Object object = new Object();
                    object.setName(feed.getName());
                    object.setDescription(feed.getDescription());
                    object.setUrl(new URL(feed.getLink()));
                    activity.setObject(object);
                    Context context = new Context();
                    context.setUsername(userId);
                    context.setDate(new DateTime(feed.getCreatedTime()));
                    context.setService("facebook");
                    activity.setContext(context);
                    activities.add(activity);
                    LOGGER.debug("FEED CONVERTED: " + activity.toString());
                } catch (MalformedURLException e) {
                    LOGGER.error("the url is malformed");
                }
            }
        }
        return activities;
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