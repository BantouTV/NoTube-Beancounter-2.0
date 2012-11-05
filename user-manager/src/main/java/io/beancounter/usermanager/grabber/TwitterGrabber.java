package io.beancounter.usermanager.grabber;

import io.beancounter.commons.helper.PropertiesHelper;
import io.beancounter.commons.model.User;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.Context;
import io.beancounter.commons.model.activity.ResolvedActivity;
import io.beancounter.commons.model.activity.Tweet;
import io.beancounter.commons.model.activity.Verb;
import io.beancounter.commons.model.auth.OAuthAuth;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.HashtagEntity;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.URLEntity;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Alex Cowell
 */
public final class TwitterGrabber implements ActivityGrabber {

    private static final Logger LOG = LoggerFactory.getLogger(TwitterGrabber.class);
    private static final String TWITTER_BASE_URL = "http://twitter.com/";
    private static final TwitterFactory TWITTER_FACTORY;

    static {
        Properties properties = PropertiesHelper.readFromClasspath("/beancounter.properties");
        String consumerKey = properties.getProperty("service.twitter.apikey");
        String consumerSecret = properties.getProperty("service.twitter.secret");

        if (!checkProperty(consumerKey) || !checkProperty(consumerSecret)) {
            throw new IllegalArgumentException("Twitter OAuth Consumer API settings are not valid. "
                    + "Please check your beancounter.properties file.");
        }

        Configuration configuration = new ConfigurationBuilder()
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .build();

        TWITTER_FACTORY = new TwitterFactory(configuration);
    }

    private final User user;
    private final String serviceUserId;
    private final int limit;

    public static TwitterGrabber create(User user, String serviceUserId) {
        return new TwitterGrabber(user, serviceUserId, 10);
    }

    TwitterGrabber(User user, String serviceUserId, int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be at least 1");
        }

        if (user.getAuth("twitter") == null) {
            throw new IllegalArgumentException("User [" + user.getUsername() + "] does not have Twitter authentication");
        }

        this.user = user;
        this.serviceUserId = serviceUserId;
        this.limit = limit;
    }

    @Override
    public List<ResolvedActivity> grab() {
        Twitter twitter = TWITTER_FACTORY.getInstance();
        OAuthAuth auth = (OAuthAuth) user.getAuth("twitter");
        twitter.setOAuthAccessToken(new AccessToken(auth.getSession(), auth.getSecret()));

        List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>();
        ResponseList<Status> statuses;
        try {
            Paging paging = new Paging(1, limit);
            statuses = twitter.getUserTimeline("", paging);
        } catch (TwitterException twx) {
            // TODO: Consider throwing an exception.
            LOG.error("Error while getting tweets for user [{}]", serviceUserId, twx);
            return activities;
        }

        for (Status status : statuses) {
            Activity activity;
            try {
                activity = convert(status);
            } catch (Exception ex) {
                LOG.warn("Error while converting tweet to beancounter activity: {}", status, ex);
                continue;
            }
            activities.add(new ResolvedActivity(user.getId(), activity, user));
        }

        return activities;
    }

    private Activity convert(Status status) throws MalformedURLException {
        String tweetUrl = TWITTER_BASE_URL + status.getUser().getName() + "/status/" + status.getId();

        Tweet tweet = new Tweet();
        tweet.setUrl(new URL(tweetUrl));
        tweet.setText(status.getText());
        for (HashtagEntity ht : status.getHashtagEntities()) {
            tweet.addHashTag(ht.getText());
        }
        for (URLEntity urlEntity : status.getURLEntities()) {
            tweet.addUrl(urlEntity.getExpandedURL());
        }

        Context context = new Context();
        context.setService("twitter");
        context.setUsername(serviceUserId);
        context.setDate(new DateTime(status.getCreatedAt().getTime()));

        return new Activity(Verb.TWEET, tweet, context);
    }

    private static boolean checkProperty(String property) {
        return property != null && !property.trim().isEmpty();
    }
}
