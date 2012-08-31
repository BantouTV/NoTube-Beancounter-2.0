package io.beancounter.publisher.twitter;

import io.beancounter.commons.model.activity.Verb;
import io.beancounter.commons.model.activity.rai.Comment;
import io.beancounter.publisher.twitter.adapters.CommentPublisher;
import io.beancounter.publisher.twitter.adapters.Publisher;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Enrico Candino ( enrico.candino @ gmail.com )
 */
public class TwitterPublisherTest {

    private Twitter twitter;

    @BeforeSuite
    public void setUp() {
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

    @Test(enabled = false)
    public void testComment() throws MalformedURLException, TwitterPublisherException {
        Comment comment = new Comment(new URL("http://google.com"), "Just a comment");
        Publisher<Comment> publisher = new CommentPublisher();
        publisher.publish(twitter, Verb.COMMENT, comment);
    }

    @Test(enabled = false)
    public void testLongComment() throws MalformedURLException, TwitterPublisherException {
        String longComment = "Bacon ipsum dolor sit amet meatball short ribs filet mignon kielbasa tenderloin " +
                "pork pork belly sirloin bacon corned beef. Jowl pork belly venison chicken fatback pancetta " +
                "ribeye kielbasa andouille bacon. Bresaola cow meatloaf, boudin fatback tenderloin chicken filet " +
                "mignon flank pork tail sausage. Meatloaf short loin shoulder cow spare ribs. Pastrami salami " +
                "jerky bresaola, flank leberkas shoulder ham beef ribs. Rump pancetta chuck meatball short ribs, " +
                "pork belly pork loin ham flank ham hock swine bresaola ribeye. Strip steak bresaola flank chicken, " +
                "short ribs tenderloin hamburger fatback swine corned beef.";

        Comment comment = new Comment(new URL("http://baconipsum.com"), longComment);
        Publisher<Comment> publisher = new CommentPublisher();
        publisher.publish(twitter, Verb.COMMENT, comment);
    }

    @Test(enabled = false)
    public void testLongUrl() throws MalformedURLException, TwitterPublisherException {
        URL longUrl = new URL("http://www.freakinghugeurl.com/refer.php?count=12&url=Vm0wd2QyUXlVWGxXYTJoV1YwZG9WV" +
                "ll3Wkc5alJsWjBUVlpPV0Zac2JETlhhMUpUVmpGYWMySkVUbGhoTWsweFZqQmFTMk15U2tWVWJHaG9UVmhDVVZadGVGWmxSbGw" +
                "1Vkd0c2FsSnRhRzlVVjNOM1pVWmFkR05GZEZSTlZUVkpWbTEwYTFkSFNrZGpTRUpYWVRGYWFGVXhXbXRXTVhCRlZXeFNUbUY2" +
                "UlRCV2EyTXhWREZrU0ZOclpHcFRSVXBZV1ZSR2QyRkdjRmRYYlVaclVqRmFTVnBGV2xOVWJGcFlaSHBDVjJFeVRYaFdSRVpyV" +
                "TBaT2NscEhjRk5XUjNob1YxZDRVMUl5VW5OWGEyUllZbGhTV1ZWcVJrdFRWbkJHVjJ4T1ZXSkdjRlpXYlhoelZqRmFObEZZYUZ" +
                "abGEzQklXWHBHVDJSV1duTlRiV3hUVFRKb1dWWnJXbGRaVm14WFZXdGtWMWRIYUZsWmJHaFRWMFpTVjFwR1RrNVNia0pIVmpKN" +
                "FQxWlhTa2RqUkVaV1ZtMW9jbFpxUm1GU2JVbDZXa1prYUdFeGNHaFhiRnBoVkRKT2MyTkZaR2hTTW1oeldXeG9iMWRzV1hoWGJ" +
                "YUk9VbXRzTTFSc1ZtdFdiVXB5WTBac1dtSkhhRlJXTUZwVFZqRndSMVJyTlZOaVJtOTNWMnhXWVZReVJrZFhiazVxVTBoQ1lWU" +
                "lZXbUZsYkZweFUydGthbUpWVmpaWlZWcGhZVWRGZUdOR2FGaGlSbkJvVmtSS1QyUkdTbkpoUjJoVFlYcFdkMVp0Y0V0aU1XUlh" +
                "WMWhvWVZKRlNtOVVWbHBYVFRGU2MyRkZPV2hpUlhCNldUQmFjMWR0U2toaFJsSmFUVlp3ZWxreWVHdGtWbkJJWlVaa2FWSXpZM" +
                "2hXTW5oWFZqRlJlRmRZWkU1WFJYQllXVmR6TVZsV1VsWlhibVJYVW14d2VGVnRNVWRXTURGeVRsVm9WMUo2UmtoV1ZFWkxWakp" +
                "PUmxac1pHbFNNVVYzVmxaU1IxbFdXbkpOVmxwWFlYcFdWRlZyVmtaT1VUMDk=");

        Comment comment = new Comment(longUrl, "This is short.");
        Publisher<Comment> publisher = new CommentPublisher();
        publisher.publish(twitter, Verb.COMMENT, comment);
    }

}
