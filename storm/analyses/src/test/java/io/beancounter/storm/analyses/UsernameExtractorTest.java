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
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
public class UsernameExtractorTest {

    private UsernameExtractor boltUnderTest;

    private OutputCollector collector;
    private Tuple tuple;
    private ObjectMapper mapper;

    @BeforeMethod
    public void setUp() throws Exception {
        collector = mock(OutputCollector.class);
        tuple = mock(Tuple.class);
        mapper = new ObjectMapper();

        boltUnderTest = new UsernameExtractor();
    }

    @Test
    public void shouldExtractUsernameFromActivity() throws Exception {
        String activityJson = mapper.writeValueAsString(getActivity("test-user"));
        when(tuple.getString(0)).thenReturn(activityJson);

        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector).emit(new Values("test-user"));
    }

    @Test
    public void shouldAckAndDropTupleIfUsernameIsNotPresent() throws Exception {
        String activityJson = mapper.writeValueAsString(getActivity(null));
        when(tuple.getString(0)).thenReturn(activityJson);

        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector, never()).emit(any(Values.class));
    }

    @Test
    public void shouldAckAndDropTupleIfMessageIsNotActivity() throws Exception {
        String activityJson = "{\"not\": true, \"activity\": 123}";
        when(tuple.getString(0)).thenReturn(activityJson);

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
        assertEquals(declaredFields.toList(), new Fields("username").toList());
    }

    private Activity getActivity(String username) throws Exception {
        Context context = new Context(DateTime.now());
        context.setService("http://twitter.com");
        context.setUsername(username);

        return new Activity(Verb.TWEET, new Tweet(), context);
    }
}
