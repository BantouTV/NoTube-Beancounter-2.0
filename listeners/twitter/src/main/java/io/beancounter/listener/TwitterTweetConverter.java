package io.beancounter.listener;

import io.beancounter.commons.model.activity.Activity;
import io.beancounter.listener.model.ServiceResponseException;
import io.beancounter.listener.model.TwitterResponse;
import io.beancounter.listener.model.TwitterTweet;

import java.util.ArrayList;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class TwitterTweetConverter {

    public Activity convert(TwitterTweet tweet) throws ServiceResponseException {
        ArrayList<TwitterTweet> tweets = new ArrayList<TwitterTweet>();
        tweets.add(tweet);
        TwitterResponse response = new TwitterResponse(tweets);
        Activity result = response.getResponse().get(0);
        result.getContext().setService("twitter");
        result.getContext().setUsername(tweet.getUserId());
        return result;
    }

}