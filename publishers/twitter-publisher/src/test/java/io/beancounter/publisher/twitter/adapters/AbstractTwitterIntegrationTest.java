package io.beancounter.publisher.twitter.adapters;

import org.testng.annotations.BeforeSuite;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 *
 * @author Enrico Candino ( enrico.candino @ gmail.com )
 */
public abstract class AbstractTwitterIntegrationTest {

    protected long timestamp = System.currentTimeMillis();

    protected Twitter twitter;

    protected Publisher publisher;

    @BeforeSuite
    public void setUp() {
        initializeTwitter();
    }

    private void initializeTwitter() {
        // NOTE: these are for testing porpouse. Do not use them in production!
        String consumerKey = "jmnTg5WJIgrQZQaVMadUg";
        String consumerSecret = "R2zTwXd5FgzYK6XjxnlzBnD4wmNpcB9IrPAPuQ5kE";

        String token = "776144576-NaZXWXdy9DpxeiLYIx4gjcDegTA9EPwvo6Jn0cTv";
        String tokenSecret = "8jvhWONYXpRmCaA5SNOj47KQkvEum5VdZpo9iEeXXwY";

        TwitterFactory factory = new TwitterFactory();
        twitter = factory.getInstance();
        twitter.setOAuthConsumer(consumerKey, consumerSecret);
        twitter.setOAuthAccessToken(new AccessToken(token, tokenSecret));
    }
}
