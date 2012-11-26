package io.beancounter.storm.analyses;

import backtype.storm.spout.KestrelThriftClient;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.Tweet;
import org.apache.thrift7.TException;
import org.codehaus.jackson.map.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import storm.redis.JedisPoolConfigSerializable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class KestrelBolt extends BaseRichBolt {

    private String address;

    private int port;

    private String queue;

    private Map map;

    private TopologyContext topologyContext;

    private OutputCollector outputCollector;

    private KestrelThriftClient kestrelClient;

    private ObjectMapper mapper;

    public KestrelBolt(String address, int port, String queue) {
        this.address = address;
        this.port = port;
        this.queue = queue;
    }

    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.map = map;
        this.topologyContext = topologyContext;
        this.outputCollector = outputCollector;
        try {
            kestrelClient = new KestrelThriftClient(address, port);
        } catch (TException e) {
            throw new RuntimeException("can't connect to Kestrel using [" + address + ":" + port + "]", e);
        }
        mapper = new ObjectMapper();
    }

    public void execute(Tuple tuple) {
        String keyword = tuple.getString(1);
        int mentions = Integer.valueOf(tuple.getString(2));
        outputCollector.ack(tuple);
        String jsonValue = getJsonValue(keyword, mentions);
        try {
            kestrelClient.put(queue, jsonValue, Integer.MAX_VALUE);
        } catch (TException e) {
            throw new RuntimeException("can't push [" + jsonValue + "] to Kestrel using [" + address + ":" + port + "]", e);
        }
    }

    private String getJsonValue(String keyword, int mentions) {
        Map<String, Integer> mentionsMap = new HashMap<String, Integer>();
        mentionsMap.put(keyword, mentions);
        try {
            return mapper.writeValueAsString(mentionsMap);
        } catch (IOException e) {
            throw new RuntimeException("error while serializing in JSON", e);
        }
    }

    public void closeKestrelConnection() {
        kestrelClient.close();
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("json"));
    }
}
