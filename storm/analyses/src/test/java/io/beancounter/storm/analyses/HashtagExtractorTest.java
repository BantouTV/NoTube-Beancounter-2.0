package io.beancounter.storm.analyses;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.Context;
import io.beancounter.commons.model.activity.Tweet;
import io.beancounter.commons.model.activity.Verb;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Alex Cowell
 */
public class HashtagExtractorTest {

    private HashtagExtractor boltUnderTest;

    private OutputCollector collector;
    private Tuple tuple;
    private ObjectMapper mapper;

    @BeforeMethod
    public void setUp() throws Exception {
        collector = mock(OutputCollector.class);
        tuple = mock(Tuple.class);

        mapper = new ObjectMapper();

        boltUnderTest = new HashtagExtractor();
    }

    @Test
    public void shouldNotEmitAnythingWhenThereAreNoHashtags() throws Exception {
        String tweetJson = mapper.writeValueAsString(getActivity());
        when(tuple.getString(0)).thenReturn(tweetJson);

        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector, never()).emit(any(Values.class));
    }

    @Test
    public void shouldEmitSingleHashtag() throws Exception {
        String tweetJson = mapper.writeValueAsString(getActivity("singlehashtag"));
        when(tuple.getString(0)).thenReturn(tweetJson);

        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector).emit(new Values("singlehashtag"));
    }

    @Test
    public void shouldEmitMultipleHashtags() throws Exception {
        String tweetJson = mapper.writeValueAsString(getActivity("hashtag1", "hashtag2"));
        when(tuple.getString(0)).thenReturn(tweetJson);

        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector).emit(new Values("hashtag1"));
        verify(collector).emit(new Values("hashtag2"));
    }

    @Test
    public void activityWhichIsNotATweetShouldBeAckedAndDropped() throws Exception {
        Activity activity = getActivity("Will be dropped.");
        activity.setObject(new Like());
        String invalidJson = mapper.writeValueAsString(activity);
        when(tuple.getString(0)).thenReturn(invalidJson);

        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector, never()).emit(any(Values.class));
    }

    @Test
    public void shouldDeclareCorrectOutputFields() throws Exception {
        OutputFieldsDeclarer declarer = mock(OutputFieldsDeclarer.class);
        ArgumentCaptor<Fields> fieldsCaptor = ArgumentCaptor.forClass(Fields.class);
        doNothing().when(declarer).declare(fieldsCaptor.capture());

        boltUnderTest.declareOutputFields(declarer);

        verify(declarer).declare(any(Fields.class));
        Fields declaredFields = fieldsCaptor.getValue();
        assertEquals(declaredFields.toList(), new Fields("hashtag").toList());
    }

    private Activity getActivity(String... hashtags) throws Exception {
        Tweet tweet = new Tweet();
        tweet.setUrl(new URL("http://twitter.com/test-user/status/123456"));
        tweet.setName("Test User");
        tweet.setText("This is a test tweet!");
        for (String hashtag : hashtags) {
            tweet.addHashTag(hashtag);
        }

        Context context = new Context(DateTime.now());
        context.setService("http://twitter.com");
        context.setUsername("test-user");

        return new Activity(Verb.TWEET, tweet, context);
    }
}
