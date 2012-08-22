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
        // TODO (med) this is a workaround to bypass the resolver in our sally demo.
        // until the sally demo won't be better engineered.
        result.getContext().setService("twitter");
        result.getContext().setUsername("sally-beancounter");
        return result;
    }

}