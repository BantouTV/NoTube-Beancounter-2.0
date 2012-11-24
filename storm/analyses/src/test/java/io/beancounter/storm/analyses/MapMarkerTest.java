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
public class MapMarkerTest {

    private OutputCollector collector;

    private MapMarker boltUnderTest;

    @BeforeMethod
    public void setUp() throws Exception {
        collector = mock(OutputCollector.class);
        boltUnderTest = new MapMarker();
    }

    @Test
    public void textWithJustOneEconomyKeywordShouldBePutInEconomyCategory() throws Exception {
        Tuple tuple = mockTuple(8.0, 40.3, "Fix the economy!");

        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector).emit(new Values(8.0, 40.3, "economy"));
    }

    @Test
    public void textWithJustTwoEconomyKeywordsShouldBePutInEconomyCategory() throws Exception {
        Tuple tuple = mockTuple(8.0, 40.3, "This economy...how can we stimulate the economy?");

        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector).emit(new Values(8.0, 40.3, "economy"));
    }

    @Test
    public void textWithJustOneEnvironmentKeywordShouldBePutInEnvironmentCategory() throws Exception {
        Tuple tuple = mockTuple(8.0, 40.3, "Save the environment!");

        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector).emit(new Values(8.0, 40.3, "environment"));
    }

    @Test
    public void textWithJustTwoEnvironmentKeywordsShouldBePutInEnvironmentCategory() throws Exception {
        Tuple tuple = mockTuple(8.0, 40.3, "Save the environment! Stop climate change!");

        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector).emit(new Values(8.0, 40.3, "environment"));
    }

    @Test
    public void textShouldBeCorrectlyCategorisedRegardlessOfCase() throws Exception {
        Tuple tuple = mockTuple(8.0, 40.3, "Fix the ECONomy!");

        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector).emit(new Values(8.0, 40.3, "economy"));
    }

    @Test
    public void economyKeywordSynonymsShouldBeUsedToCategoriseText() throws Exception {
        Tuple tuple = mockTuple(8.0, 40.3, "What will the new budget be?");

        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector).emit(new Values(8.0, 40.3, "economy"));
    }

    @Test
    public void environmentKeywordSynonymsShouldBeUsedToCategoriseText() throws Exception {
        Tuple tuple = mockTuple(8.0, 40.3, "Stop climate change!");

        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector).emit(new Values(8.0, 40.3, "environment"));
    }

    @Test
    public void textWithNoKeywordsShouldBeMarkedAsOther() throws Exception {
        Tuple tuple = mockTuple(8.0, 40.3, "A message which cannot be categorised");

        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector).emit(new Values(8.0, 40.3, "other"));
    }

    @Test
    public void textWithMostlyEconomyKeywordsShouldBeCategorisedAsEconomy() throws Exception {
        Tuple tuple = mockTuple(8.0, 40.3, "I want to know about the economy, budget and environment.");

        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector).emit(new Values(8.0, 40.3, "economy"));
    }

    @Test
    public void textWithMostlyEnvironmentKeywordsShouldBeCategorisedAsEnvironment() throws Exception {
        Tuple tuple = mockTuple(8.0, 40.3, "I care more about the environment and climate change than the economy.");

        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector).emit(new Values(8.0, 40.3, "environment"));
    }

    @Test
    public void textWithEqualCountsOfKeywordsShouldResolveIntoOneCategory() throws Exception {
        Tuple tuple = mockTuple(8.0, 40.3, "I care equally about the environment and the economy.");

        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector).emit(new Values(8.0, 40.3, "environment"));
    }

    @Test
    public void textWithItalianKeywordShouldBeCorrectlyCategorised() throws Exception {
        Tuple tuple = mockTuple(8.0, 40.3, "Something about università.");

        boltUnderTest.prepare(Collections.emptyMap(), mock(TopologyContext.class), collector);
        boltUnderTest.execute(tuple);

        verify(collector).ack(tuple);
        verify(collector).emit(new Values(8.0, 40.3, "education"));
    }

    @Test
    public void shouldDeclareCorrectOutputFields() throws Exception {
        OutputFieldsDeclarer declarer = mock(OutputFieldsDeclarer.class);
        ArgumentCaptor<Fields> fieldsCaptor = ArgumentCaptor.forClass(Fields.class);
        doNothing().when(declarer).declare(fieldsCaptor.capture());

        boltUnderTest.declareOutputFields(declarer);

        verify(declarer).declare(any(Fields.class));
        Fields declaredFields = fieldsCaptor.getValue();
        assertEquals(declaredFields.toList(), new Fields("lat", "long", "category").toList());
    }

    private Tuple mockTuple(double lat, double lon, String text) {
        Tuple tuple = mock(Tuple.class);
        when(tuple.getDouble(0)).thenReturn(lat);
        when(tuple.getDouble(1)).thenReturn(lon);
        when(tuple.getString(2)).thenReturn(text);
        return tuple;
    }
}
