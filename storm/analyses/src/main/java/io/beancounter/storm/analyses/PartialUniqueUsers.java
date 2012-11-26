package io.beancounter.storm.analyses;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This Bolt will keep track of a subset of all usernames and will emit a tuple
 * when a user that hasn't been seen before arrives.
 *
 * Expects tuples of the form:
 *      [ username:string ]
 *
 * and will emit a tuple of the form:
 *      [ username:string ]
 *
 * if that user has not been tracked before.
 *
 * @author Alex Cowell
 */
public class PartialUniqueUsers extends BaseRichBolt {

    private final Set<String> users;

    private OutputCollector collector;

    public PartialUniqueUsers() {
        users = new HashSet<String>();
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple tuple) {
        String username = tuple.getString(0);
        collector.ack(tuple);

        if (!users.contains(username)) {
            users.add(username);
            collector.emit(new Values(username));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("username"));
    }
}
