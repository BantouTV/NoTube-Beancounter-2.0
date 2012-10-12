package io.beancounter.listener;

import java.net.MalformedURLException;
import java.net.URL;

import org.joda.time.DateTime;

import io.beancounter.listener.model.TwitterTweet;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.URLEntity;

/**
 *
 */
public class TweetConverter {

    public TwitterTweet convert(Status status) {

        TwitterTweet tweet = new TwitterTweet();

        tweet.setCreatedAt(new DateTime(status.getCreatedAt()));

        tweet.setText(status.getText());

        String twitterId = String.valueOf(status.getUser().getId());
        String screenName = status.getUser().getScreenName();
        tweet.setUserId(twitterId);

        try {
            URL tweetUrl = new URL("http://twitter.com/" + screenName + "/status/" + twitterId);
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
        return tweet;
    }
}
