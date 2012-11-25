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
import java.util.Locale;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Alex Cowell
 */
public class WordSplitterTest {

    private WordSplitter boltUnderTest;

    private OutputCollector collector;
    private Tuple tuple;
    private ObjectMapper mapper;

    @BeforeMethod
    public void setUp() throws Exception {
        collector = mock(OutputCollector.class);
        tuple = mock(Tuple.class);

        mapper = new ObjectMapper();
    }

    @Test
    public void shouldSplitWordsAccordingToEnglishLocale() throws Exception {
        String tweetJson = mapper.writeValueAsString(getActivity("This is a test tweet!"));
        when(tuple.getString(0)).thenReturn(tweetJson);

        boltUnderTest = new WordSplitter(Locale.ENGLISH);
        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector, times(5)).emit(any(Values.class));
        verify(collector).emit(new Values("this"));
        verify(collector).emit(new Values("is"));
        verify(collector).emit(new Values("a"));
        verify(collector).emit(new Values("test"));
        verify(collector).emit(new Values("tweet"));
    }

    @Test
    public void shouldSplitWordsAccordingToItalianLocale() throws Exception {
        String tweetJson = mapper.writeValueAsString(getActivity("Privatizzazione dell'acqua."));
        when(tuple.getString(0)).thenReturn(tweetJson);

        boltUnderTest = new WordSplitter(Locale.ITALY);
        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector, times(2)).emit(any(Values.class));
        verify(collector).emit(new Values("privatizzazione"));
        verify(collector).emit(new Values("dell'acqua"));
    }

    @Test
    public void activityWhichIsNotATweetShouldBeAckedAndDropped() throws Exception {
        Activity activity = getActivity("Will be dropped.");
        activity.setObject(new Like());
        String invalidJson = mapper.writeValueAsString(activity);
        when(tuple.getString(0)).thenReturn(invalidJson);

        boltUnderTest = new WordSplitter(Locale.ENGLISH);
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

        boltUnderTest = new WordSplitter(null);
        boltUnderTest.declareOutputFields(declarer);

        verify(declarer).declare(any(Fields.class));
        Fields declaredFields = fieldsCaptor.getValue();
        assertEquals(declaredFields.toList(), new Fields("word").toList());
    }

    private Activity getActivity(String text) throws Exception {
        Tweet tweet = new Tweet();
        tweet.setUrl(new URL("http://twitter.com/test-user/status/123456"));
        tweet.setName("Test User");
        tweet.setText(text);

        Context context = new Context(DateTime.now());
        context.setService("http://twitter.com");
        context.setUsername("test-user");

        return new Activity(Verb.TWEET, tweet, context);
    }
}
