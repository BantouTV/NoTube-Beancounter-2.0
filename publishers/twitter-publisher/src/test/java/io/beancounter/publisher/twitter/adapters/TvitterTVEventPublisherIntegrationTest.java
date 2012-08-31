package io.beancounter.publisher.twitter.adapters;

import io.beancounter.commons.model.activity.Verb;
import io.beancounter.commons.model.activity.rai.TVEvent;
import io.beancounter.publisher.twitter.TwitterPublisherException;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 *
 * @author Enrico Candino ( enrico.candino @ gmail.com )
 */
public class TvitterTVEventPublisherIntegrationTest extends AbstractTwitterIntegrationTest {

    @BeforeSuite
    @Override
    public void setUp() {
        super.setUp();
        publisher = new TVEventPublisher();
    }

    @Test
    public void testWatchedTVEvent() throws MalformedURLException, TwitterPublisherException {
        TVEvent tvEvent = getTVEvent();
        publisher.publish(twitter, Verb.WATCHED, tvEvent);
    }

    @Test
    public void testCheckinTVEvent() throws MalformedURLException, TwitterPublisherException {
        TVEvent tvEvent = getTVEvent();
        publisher.publish(twitter, Verb.CHECKIN, tvEvent);
    }

    @Test
    public void testOtherTVEvent() throws MalformedURLException, TwitterPublisherException {
        TVEvent tvEvent = getTVEvent();
        publisher.publish(twitter, Verb.FAVORITED, tvEvent);
    }

    private TVEvent getTVEvent() throws MalformedURLException {
        UUID id = UUID.randomUUID();
        TVEvent tvEvent = new TVEvent();
        tvEvent.setId(id);
        tvEvent.setOnEvent("ContentItem-" + id.toString());
        tvEvent.setName("[" + timestamp + "] Twitter TVEvent");
        tvEvent.setUrl(new URL("http://www.google.com"));
        tvEvent.setDescription("A testing TVEvent for twitter!");
        return tvEvent;
    }
}
