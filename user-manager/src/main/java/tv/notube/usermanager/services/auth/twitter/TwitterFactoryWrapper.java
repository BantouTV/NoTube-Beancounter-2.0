package tv.notube.usermanager.services.auth.twitter;

import com.google.inject.Inject;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

public class TwitterFactoryWrapper {

    private TwitterFactory twitterFactory;

    @Inject
    public TwitterFactoryWrapper(TwitterFactory twitterFactory) {
        this.twitterFactory = twitterFactory;
    }

    public Twitter getInstance() {
        return twitterFactory.getInstance();
    }
}
