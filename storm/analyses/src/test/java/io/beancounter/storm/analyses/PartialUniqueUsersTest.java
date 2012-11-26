package io.beancounter.storm.analyses;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Alex Cowell
 */
public class PartialUniqueUsersTest {

    @Test
    public void shouldEmitUsernameOfNewUniqueUser() throws Exception {
        OutputCollector collector = mock(OutputCollector.class);
        Tuple tuple = mock(Tuple.class);
        when(tuple.getString(0)).thenReturn("new-user");

        PartialUniqueUsers boltUnderTest = new PartialUniqueUsers();
        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector).emit(new Values("new-user"));
    }

    @Test
    public void shouldNotEmitUsernameOfNonUniqueUser() throws Exception {
        OutputCollector collector = mock(OutputCollector.class);
        Tuple tuple = mock(Tuple.class);
        when(tuple.getString(0)).thenReturn("test-user");

        PartialUniqueUsers boltUnderTest = new PartialUniqueUsers();
        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);

        boltUnderTest.execute(tuple);
        boltUnderTest.execute(tuple);

        verify(collector, times(2)).ack(tuple);
        verify(collector).emit(new Values("test-user"));
    }

    @Test
    public void shouldDeclareCorrectOutputFields() throws Exception {
        OutputFieldsDeclarer declarer = mock(OutputFieldsDeclarer.class);
        ArgumentCaptor<Fields> fieldsCaptor = ArgumentCaptor.forClass(Fields.class);
        doNothing().when(declarer).declare(fieldsCaptor.capture());

        PartialUniqueUsers boltUnderTest = new PartialUniqueUsers();
        boltUnderTest.declareOutputFields(declarer);

        verify(declarer).declare(any(Fields.class));
        Fields declaredFields = fieldsCaptor.getValue();
        assertEquals(declaredFields.toList(), new Fields("username").toList());
    }
}
