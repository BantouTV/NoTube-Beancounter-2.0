package io.beancounter.publisher.twitter.adapters;

import io.beancounter.commons.model.activity.Object;
import io.beancounter.commons.model.activity.Verb;
import io.beancounter.publisher.twitter.TwitterPublisherException;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Enrico Candino (( enrico.candino@gmail.com ))
 */
public class TwitterObjectPublisherIntegrationTest extends AbstractTwitterIntegrationTest {

    @BeforeSuite
    @Override
    public void setUp() {
        super.setUp();
        publisher = new ObjectPublisher();
    }

    @Test
     public void testObject() throws MalformedURLException, TwitterPublisherException {
        Object object = new Object(new URL("http://google.com"));
        object.setDescription("[" + timestamp + " OBJECT] Just an object description");
        publisher.publish(twitter, Verb.LIKE, object);
    }

    @Test
    public void testLongDescriptionObject() throws MalformedURLException, TwitterPublisherException {
        String longText = "cask conditioning pint glass hard cider amber. alcohol krug lauter tun carbonation. " +
                "berliner weisse lauter tun enzymes length real ale enzymes. autolysis double bock/dopplebock-- " +
                "hop back; scotch ale hop back gravity lambic, brewing length goblet wit barley. dunkle fermentation, " +
                "additive fermentation wort attenuation hops barrel, yeast krausen: reinheitsgebot.";

        Object object = new Object(new URL("https://google.com"));
        object.setDescription("[" + timestamp + " OBJECT - LONG DESCRIPTION] " + longText);
        publisher.publish(twitter, Verb.LIKE, object);
    }

    @Test
    public void testLongUrlObject() throws MalformedURLException, TwitterPublisherException {
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

        Object object = new Object(longUrl);
        object.setDescription("[" + timestamp + " OBJECT - LONG URL] Just a short text.");
        publisher.publish(twitter, Verb.LIKE, object);
    }

}
