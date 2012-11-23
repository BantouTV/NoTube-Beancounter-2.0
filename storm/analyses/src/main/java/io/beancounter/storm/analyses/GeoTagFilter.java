package io.beancounter.storm.analyses;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.Coordinates;
import io.beancounter.commons.model.activity.Tweet;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.Map;

/**
 * Simple bolt to drop any Tweets which do not contain geo-location data.
 *
 * For Tweets with location data, it will emit a tuple containing:
 *      [ lat, long, text ]
 *
 * @author Alex Cowell
 */
public class GeoTagFilter extends BaseRichBolt {

    private OutputCollector collector;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple tuple) {
        String tweetJson = tuple.getString(0);
        collector.ack(tuple);

        Tweet tweet;
        try {
            tweet = (Tweet) new ObjectMapper().readValue(tweetJson, Activity.class).getObject();
        } catch (Exception ex) {
            return;
        }

        Coordinates coordinates = tweet.getGeo();
        if (coordinates != null) {
            collector.emit(new Values(coordinates.getLat(), coordinates.getLon(), tweet.getText()));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("lat", "long", "text"));
    }
}
