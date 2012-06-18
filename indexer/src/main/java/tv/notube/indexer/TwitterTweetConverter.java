package tv.notube.indexer;

import tv.notube.commons.model.activity.*;
import tv.notube.crawler.requester.ServiceResponseException;
import tv.notube.crawler.requester.request.twitter.TwitterResponse;
import tv.notube.crawler.requester.request.twitter.TwitterTweet;

import java.util.ArrayList;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class TwitterTweetConverter {

    public Activity convert(TwitterTweet tweet) throws ServiceResponseException {

        ArrayList<TwitterTweet> tweets = new ArrayList<TwitterTweet>();
        tweets.add(tweet);
        TwitterResponse response = new TwitterResponse(tweets);
        return response.getResponse().get(0);

    }


}