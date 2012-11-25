package io.beancounter.storm.analyses;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
public class KeywordCounterTest {

    private KeywordCounter boltUnderTest;

    private OutputCollector collector;

    @BeforeMethod
    public void setUp() throws Exception {
        collector = mock(OutputCollector.class);
    }

    @Test
    public void shouldCountSingleKeyword() throws Exception {
        Tuple tuple = mockTuple("keyword");

        boltUnderTest = new KeywordCounter("keyword");
        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector).emit(new Values("keyword", 1));
    }

    @Test
    public void shouldRepeatedlyCountSingleKeyword() throws Exception {
        Tuple tuple = mockTuple("keyword");

        boltUnderTest = new KeywordCounter("keyword");
        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);

        int numWords = 5;
        for (int i = 0; i < numWords; i++) {
            boltUnderTest.execute(tuple);
        }

        verify(collector, times(numWords)).ack(tuple);
        verify(collector, times(numWords)).emit(any(Values.class));
        for (int i = 1; i <= numWords; i++) {
            verify(collector).emit(new Values("keyword", i));
        }
    }

    @Test
    public void shouldCountMultipleKeywords() throws Exception {
        Tuple tuple = mockTuple("keyword");

        boltUnderTest = new KeywordCounter("keyword", "another-keyword");
        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);

        boltUnderTest.execute(tuple);
        verify(collector).ack(tuple);
        verify(collector).emit(new Values("keyword", 1));

        tuple = mockTuple("another-keyword");
        boltUnderTest.execute(tuple);
        verify(collector).ack(tuple);
        verify(collector).emit(new Values("another-keyword", 1));
    }

    @Test
    public void shouldNotCountWordWhichIsNotKeyword() throws Exception {
        Tuple tuple = mockTuple("not-a-keyword");

        boltUnderTest = new KeywordCounter("keyword");
        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector, never()).emit(any(Values.class));
    }

    @Test
    public void shouldCountKeywordRegardlessOfCase() throws Exception {
        Tuple tuple = mockTuple("keyword");

        boltUnderTest = new KeywordCounter("KEYWORD");
        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector).emit(new Values("keyword", 1));
    }

    @Test
    public void shouldRespectLocalWhenCountingKeywords() throws Exception {
        Tuple tuple = mockTuple("stabilità");

        boltUnderTest = new KeywordCounter(Locale.ITALIAN, "STABILITÀ");
        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector).emit(new Values("stabilità", 1));
    }

    @Test
    public void shouldDeclareCorrectOutputFields() throws Exception {
        OutputFieldsDeclarer declarer = mock(OutputFieldsDeclarer.class);
        ArgumentCaptor<Fields> fieldsCaptor = ArgumentCaptor.forClass(Fields.class);
        doNothing().when(declarer).declare(fieldsCaptor.capture());

        boltUnderTest = new KeywordCounter();
        boltUnderTest.declareOutputFields(declarer);

        verify(declarer).declare(any(Fields.class));
        Fields declaredFields = fieldsCaptor.getValue();
        assertEquals(declaredFields.toList(), new Fields("word", "count").toList());
    }

    private Tuple mockTuple(String word) {
        Tuple tuple = mock(Tuple.class);
        when(tuple.getString(0)).thenReturn(word);
        return tuple;
    }
}
