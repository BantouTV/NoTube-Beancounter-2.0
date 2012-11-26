package io.beancounter.storm.analyses;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Simply counts how many tuples have been processed. This bolt will only give
 * accurate stream data if it is a singleton.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TweetOMeterBolt extends BaseRichBolt {

    private OutputCollector collector;

    private long lastupdate = 0L;

    private LinkedList<InstantValue> precedentValues;

    /**
     * calculates the average only from the last SIZE tweets.
     */
    private static final int SIZE = 100;

    public void prepare(Map map, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        precedentValues = new LinkedList<InstantValue>();
    }

    public void execute(Tuple tuple) {
        collector.ack(tuple);
        long now = System.currentTimeMillis();
        long interval = now - lastupdate;
        if(interval == 0) interval = 1; // to avoid DBZ
        double tps = (1 / interval) * 1000;
        if(precedentValues.size() > SIZE) {
            for(int i=0; i < precedentValues.size() - SIZE; i++)
                precedentValues.removeLast();
        }
        precedentValues.add(new InstantValue(now, tps));
        double average = avg(precedentValues);
        collector.emit(new Values("_tweets_per_second_", String.valueOf(average)));
    }

    private double avg(LinkedList<InstantValue> precedentValues) {
        double sum = 0.0d;
        for(InstantValue iv : precedentValues) {
            sum += iv.getTps();
        }
        return sum / precedentValues.size();
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("key", "value"));
    }
}
