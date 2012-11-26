package io.beancounter.storm.analyses;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import java.util.Map;

/**
 * Simply counts how many tuples have been processed. This bolt will only give
 * accurate stream data if it is a singleton or operating on a global stream
 * grouping.
 *
 * @author Alex Cowell
 */
public class CounterBolt extends BaseRichBolt {

    private final static int DATABASE = 10;

    private OutputCollector collector;

    private long count = 0L;

    public void prepare(Map map, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    public void execute(Tuple tuple) {
        collector.emit(new Values(DATABASE, "overall-number-tweets", String.valueOf(count++)));
        collector.ack(tuple);
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("database", "key", "value"));
    }
}
