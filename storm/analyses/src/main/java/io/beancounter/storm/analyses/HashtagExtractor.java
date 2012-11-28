package io.beancounter.storm.analyses;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.Tweet;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.Map;

/**
 * Extracts hashtags (without the leading '#') from Tweets and emits them
 * individually in tuples of the form:
 *      [ hashtag:string ]
 *
 * @author Alex Cowell
 */
public class HashtagExtractor extends BaseRichBolt {

    private OutputCollector collector;
    private ObjectMapper mapper;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        this.collector = collector;
        mapper = new ObjectMapper();
    }

    @Override
    public void execute(Tuple tuple) {
        collector.ack(tuple);

        Tweet tweet;
        try {
            String tweetJson = tuple.getString(0);
            tweet = (Tweet) mapper.readValue(tweetJson, Activity.class).getObject();
        } catch (Exception ex) {
            return;
        }

        for (String hashtag : tweet.getHashTags()) {
            collector.emit(new Values(hashtag));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("hashtag"));
    }
}
