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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import storm.redis.JedisPoolConfigSerializable;

import java.io.IOException;
import java.util.Map;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class UniqueUsersCountBolt extends BaseRichBolt {

    private final static int DATABASE = 9;

    private static String KEY = "_unique_users_count_";

    private JedisPool pool;

    private JedisPoolConfigSerializable config;

    private String address;

    private Map map;

    private TopologyContext topologyContext;

    private OutputCollector outputCollector;

    private ObjectMapper mapper;

    public UniqueUsersCountBolt(JedisPoolConfigSerializable config, String address) {
        this.config = config;
        this.address = address;
    }

    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.map = map;
        this.topologyContext = topologyContext;
        this.outputCollector = outputCollector;
        mapper = new ObjectMapper();
        pool = new JedisPool(
                config,
                address
        );
    }

    public void execute(Tuple tuple) {
        String jsonValue = tuple.getString(0);
        outputCollector.ack(tuple);
        Activity activity;
        try {
            activity = mapper.readValue(jsonValue, Activity.class);
        } catch (IOException e) {
            return;
        }
        String username = activity.getContext().getUsername();
        if(!isPresent(username)) {
            // this user is new, then update the counter
            int oldValue = getOldValue(KEY);
            Values values = getValues(KEY, oldValue + 1);
            setNewValue(username);
            outputCollector.emit(values);
        }
    }

    private Values getValues(String keyword, int value) {
        return new Values(DATABASE, keyword, String.valueOf(value));
    }

    private boolean isPresent(String username) {
        Jedis jedis = getJedisResource(DATABASE);
        String value;
        try {
          value = jedis.get(username);
        } finally {
            pool.returnResource(jedis);
        }
        if(value == null) {
            return false;
        }
        return true;
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("database", "key", "value"));
    }

    private void setNewValue(String username) {
        Jedis jedis = getJedisResource(DATABASE);
        try {
            // just a value
            jedis.set(username, "1");
        } finally {
            pool.returnResource(jedis);
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
            if (isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            }
        }
        return jedis;
    }

}
