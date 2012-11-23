package io.beancounter.listeners;

import io.beancounter.commons.model.activity.Activity;
import io.beancounter.listeners.model.ServiceResponseException;
import io.beancounter.listeners.model.TwitterResponse;
import io.beancounter.listeners.model.TwitterTweet;

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
        return result;
    }

}