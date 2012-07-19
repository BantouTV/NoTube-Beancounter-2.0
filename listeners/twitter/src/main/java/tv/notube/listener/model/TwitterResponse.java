package tv.notube.listener.model;

import tv.notube.commons.model.activity.*;

import java.lang.Object;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TwitterResponse implements ServiceResponse<List<Activity>> {

    private final String twitter = "http://twitter.com";

    private List<TwitterTweet> twitterTweets;

    private ActivityBuilder ab;

    public TwitterResponse(List<TwitterTweet> twitterTweets) {
        this.twitterTweets = twitterTweets;
        ab = new DefaultActivityBuilder();
    }

    public List<Activity> getResponse() throws ServiceResponseException {
        List<Activity> activities = new ArrayList<Activity>();
        for (TwitterTweet twitterTweet : twitterTweets) {
            try {
                ab.push();
                ab.setVerb(Verb.TWEET);
                Map<String, Object> fields = new HashMap<String, Object>();
                fields.put("setText", twitterTweet.getText());
                ab.setObject(
                        Tweet.class,
                        twitterTweet.getUrl(),
                        twitterTweet.getName(),
                        fields
                );
                for (String hashTag : twitterTweet.getHashTags()) {
                    ab.objectSetField("addHashTag", hashTag, String.class);
                }
                for (URL url : twitterTweet.getMentionedUrls()) {
                    ab.objectSetField("addUrl", url, URL.class);
                }
                ab.setContext(
                        twitterTweet.getCreatedAt(),
                        twitter,
                        twitterTweet.getUsername()
                );
                activities.add(ab.pop());
            } catch (ActivityBuilderException e) {
                throw new ServiceResponseException("Error while building activity", e);
            }
        }
        return activities;
    }

}