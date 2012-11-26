package io.beancounter.storm.analyses;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import io.beancounter.commons.model.activity.Activity;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.Map;

/**
 * Extracts and outputs the username of an Activity:
 *      [ username:string ]
 *
 * @author Alex Cowell
 */
public class UsernameExtractor extends BaseRichBolt {

    private final ObjectMapper mapper;

    private OutputCollector collector;

    public UsernameExtractor() {
        mapper = new ObjectMapper();
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple tuple) {
        collector.ack(tuple);
        String json = tuple.getString(0);

        Activity activity;
        try {
            activity = mapper.readValue(json, Activity.class);
        } catch (Exception ex) {
            return;
        }

        String username = activity.getContext().getUsername();
        if (username != null) {
            collector.emit(new Values(username));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("username"));
    }
}
