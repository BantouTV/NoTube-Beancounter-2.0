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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import storm.redis.JedisPoolConfigSerializable;

import java.io.IOException;
import java.util.*;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class KeywordsCountBolt extends BaseRichBolt {

    private final static int DATABASE = 10;

    private String[] keywords;

    private Map map;

    private TopologyContext topologyContext;

    private OutputCollector outputCollector;

    private ObjectMapper mapper;

    private JedisPool pool;

    private JedisPoolConfigSerializable config;

    private String address;

    public KeywordsCountBolt(JedisPoolConfigSerializable config, String address, String... keywords) {
        this.keywords = keywords;
        this.config = config;
        this.address = address;
    }

    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.map = map;
        this.topologyContext = topologyContext;
        this.outputCollector = outputCollector;
        this.mapper = new ObjectMapper();
        pool = new JedisPool(
                config,
                address
        );
    }

    public void execute(Tuple tuple) {
        String jsonValue = tuple.getString(0);
        outputCollector.ack(tuple);
        Tweet tweet;
        try {
            tweet = (Tweet) mapper.readValue(jsonValue, Activity.class).getObject();
        } catch (IOException e) {
            return;
        }
        for (String keyword : keywords) {
            String text = tweet.getText();
            if (contains(text, keyword)) {
                int oldValue = getOldValue(keyword);
                Values values = getValues(keyword, oldValue + 1);
                outputCollector.emit(values);
            }
        }
    }

    private int getOldValue(String keyword) {
        Jedis jedis = getJedisResource(DATABASE);
        String value;
        try {
          value = jedis.get(keyword);
        } finally {
            pool.returnResource(jedis);
        }
        if(value == null) {
            return 0;
        }
        return Integer.valueOf(value);
    }

    private Jedis getJedisResource(int database) {
        Jedis jedis = pool.getResource();
        boolean isConnectionIssue = false;
        try {
            jedis.select(database);
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while selecting database [" + database + "]";
            throw new RuntimeException(errMsg, e);
        } catch (Exception e) {
            pool.returnResource(jedis);
            final String errMsg = "Error while selecting database [" + database + "]";
            throw new RuntimeException(errMsg, e);
        } finally {
            if(isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            }
        }
        return jedis;
    }

    private boolean contains(String text, String key) {
        String lowerCaseText = text.toLowerCase();
        String lowerCasekey = key.toLowerCase();
        return lowerCaseText.contains(lowerCasekey);
    }

    private Values getValues(String keyword, int value) {
        return new Values(DATABASE, keyword, String.valueOf(value));
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("database", "key", "value"));
    }
}
