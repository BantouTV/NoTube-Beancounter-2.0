package io.beancounter.storm.analyses;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import io.beancounter.commons.model.activity.*;
import io.beancounter.commons.model.activity.facebook.Like;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Alex Cowell
 */
public class GeoTagFilterTest {

    private OutputCollector collector;
    private Tuple tuple;
    private ObjectMapper mapper;

    private GeoTagFilter boltUnderTest;

    @BeforeMethod
    public void setUp() throws Exception {
        collector = mock(OutputCollector.class);
        tuple = mock(Tuple.class);

        boltUnderTest = new GeoTagFilter("IT");

        mapper = new ObjectMapper();
    }

    @Test
    public void tweetWithoutLocationDataShouldBeDroppedAndAcked() throws Exception {
        String tweetJson = mapper.writeValueAsString(getActivity(null));
        when(tuple.getString(0)).thenReturn(tweetJson);

        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector, never()).emit(any(Values.class));
//        verify(boltUnderTest, never()).isInCountry(any(Coordinates.class));
    }

    @Test
    public void tweetWithLocationDataAndInSpecifiedCountryShouldBeAckedAndSentOn() throws Exception {
        String tweetJson = mapper.writeValueAsString(getActivity(new Coordinates(44.2, 11.4)));
        when(tuple.getString(0)).thenReturn(tweetJson);
//        doReturn(true).when(boltUnderTest).isInCountry(any(Coordinates.class));

        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector).emit(new Values(44.2, 11.4, "This is a test tweet!"));
    }

    @Test
    public void tweetWithLocationDataAndNotInSpecifiedCountryShouldBeDropped() throws Exception {
        String tweetJson = mapper.writeValueAsString(getActivity(new Coordinates(3.2, 24.4)));
        when(tuple.getString(0)).thenReturn(tweetJson);
//        doReturn(false).when(boltUnderTest).isInCountry(any(Coordinates.class));

        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector, never()).emit(any(Values.class));
    }

    @Test
    public void activityWhichIsNotATweetShouldBeAckedAndDropped() throws Exception {
        Activity activity = getActivity(null);
        activity.setObject(new Like());
        String invalidJson = mapper.writeValueAsString(activity);
        when(tuple.getString(0)).thenReturn(invalidJson);

        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector, never()).emit(any(Values.class));
//        verify(boltUnderTest, never()).isInCountry(any(Coordinates.class));
    }

    @Test
    public void shouldDeclareCorrectOutputFields() throws Exception {
        OutputFieldsDeclarer declarer = mock(OutputFieldsDeclarer.class);
        ArgumentCaptor<Fields> fieldsCaptor = ArgumentCaptor.forClass(Fields.class);
        doNothing().when(declarer).declare(fieldsCaptor.capture());

        boltUnderTest.declareOutputFields(declarer);

        verify(declarer).declare(any(Fields.class));
        Fields declaredFields = fieldsCaptor.getValue();
        assertEquals(declaredFields.toList(), new Fields("lat", "long", "text").toList());
    }

    private Activity getActivity(Coordinates location) throws Exception {
        Tweet tweet = new Tweet();
        tweet.setUrl(new URL("http://twitter.com/test-user/status/123456"));
        tweet.setName("Test User");
        tweet.setText("This is a test tweet!");
        if (location != null) {
            tweet.setGeo(location);
        }

        Context context = new Context(DateTime.now());
        context.setService("http://twitter.com");
        context.setUsername("test-user");

        return new Activity(Verb.TWEET, tweet, context);
    }
}
