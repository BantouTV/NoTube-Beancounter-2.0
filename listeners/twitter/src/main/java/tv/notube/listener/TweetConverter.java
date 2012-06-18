package tv.notube.listener;

import java.net.MalformedURLException;
import java.net.URL;

import org.joda.time.DateTime;

import tv.notube.crawler.requester.request.twitter.TwitterTweet;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.URLEntity;

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

        return tweet;
    }
}
