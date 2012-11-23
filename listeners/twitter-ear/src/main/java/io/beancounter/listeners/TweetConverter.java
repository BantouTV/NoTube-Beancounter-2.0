package io.beancounter.listeners;

import io.beancounter.commons.model.activity.Coordinates;
import io.beancounter.listeners.model.TwitterTweet;
import org.joda.time.DateTime;
import twitter4j.*;

import java.net.MalformedURLException;
import java.net.URL;

public class TweetConverter {

    public TwitterTweet convert(Status status) {
        TwitterTweet tweet = new TwitterTweet();
        tweet.setCreatedAt(new DateTime(status.getCreatedAt()));
        tweet.setText(status.getText());
        String screenName = status.getUser().getScreenName();
        tweet.setUsername(screenName);
        try {
            URL tweetUrl = new URL("http://twitter.com/" + screenName + "/status/" + status.getId());
            tweet.setUrl(tweetUrl);
        } catch (MalformedURLException e) {
            // leave it null
        }
        for (URLEntity urlEntity : status.getURLEntities()) {
            tweet.addUrl(urlEntity.getExpandedURL());
        }
        for (HashtagEntity hashtagEntity : status.getHashtagEntities()) {
            tweet.addHashTag(hashtagEntity.getText());
        }
        GeoLocation geoLocation = status.getGeoLocation();
        if(geoLocation != null) {
            tweet.setCoords(new Coordinates<Double, Double>(
                    geoLocation.getLatitude(),
                    geoLocation.getLongitude())
            );
        }
        for(UserMentionEntity mention : status.getUserMentionEntities()) {
            tweet.addMentionedUser(mention.getScreenName());
        }
        return tweet;
    }
}
