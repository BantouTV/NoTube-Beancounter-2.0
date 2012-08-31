package io.beancounter.publisher.twitter.adapters;

import io.beancounter.commons.model.activity.Verb;
import io.beancounter.commons.model.activity.rai.Comment;
import io.beancounter.publisher.twitter.TwitterPublisherException;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Enrico Candino ( enrico.candino @ gmail.com )
 */
public class TwitterCommentPublisherIntegrationTest extends AbstractTwitterIntegrationTest {

    @BeforeSuite
    @Override
    public void setUp() {
        super.setUp();
        publisher = new CommentPublisher();
    }

    @Test
    public void testComment() throws MalformedURLException, TwitterPublisherException {
        Comment comment = new Comment(new URL("http://google.com"), "[" + timestamp + " COMMENT] Just a comment");
        publisher.publish(twitter, Verb.COMMENT, comment);
    }

    @Test
    public void testLongComment() throws MalformedURLException, TwitterPublisherException {
        String longComment = "[" + timestamp + " - COMMENT - LONG] Bacon ipsum dolor sit amet meatball short ribs filet mignon kielbasa tenderloin " +
                "pork pork belly sirloin bacon corned beef. Jowl pork belly venison chicken fatback pancetta " +
                "ribeye kielbasa andouille bacon. Bresaola cow meatloaf, boudin fatback tenderloin chicken filet " +
                "mignon flank pork tail sausage. Meatloaf short loin shoulder cow spare ribs. Pastrami salami " +
                "jerky bresaola, flank leberkas shoulder ham beef ribs. Rump pancetta chuck meatball short ribs, " +
                "pork belly pork loin ham flank ham hock swine bresaola ribeye. Strip steak bresaola flank chicken, " +
                "short ribs tenderloin hamburger fatback swine corned beef.";

        Comment comment = new Comment(new URL("http://baconipsum.com"), longComment);
        publisher.publish(twitter, Verb.COMMENT, comment);
    }

    @Test
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

        Comment comment = new Comment(longUrl, "[" + timestamp + " - COMMENT - LONG URL] Short one.");
        publisher.publish(twitter, Verb.COMMENT, comment);
    }

}
