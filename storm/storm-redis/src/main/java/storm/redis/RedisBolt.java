package storm.redis;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.IBasicBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import org.apache.commons.pool.impl.GenericObjectPool;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Map;

/**
 * This {@link IBasicBolt} implementation allows you to store data on <i>Redis</i>.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class RedisBolt extends BaseRichBolt {

    public static final String DATABASE = "database";

    public static final String CHANNEL = "mentions";

    public static final String KEY = "key";

    public static final String VALUE = "value";

    private JedisPoolConfigSerializable config;

    private String address;

    private JedisPool pool;

    private Map map;

    private boolean isNotifying;

    private TopologyContext topologyContext;

    private OutputCollector outputCollector;

    public RedisBolt(JedisPoolConfigSerializable config, String address, boolean isNotifying) {
        this.config = config;
        this.address = address;
        this.isNotifying = isNotifying;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.map = map;
        this.topologyContext = topologyContext;
        this.outputCollector = outputCollector;
        pool = new JedisPool(
                config,
                address
        );
    }

    @Override
    public void execute(Tuple tuple) {
        int database = tuple.getIntegerByField(DATABASE);
        String key = tuple.getStringByField(KEY);
        String value = tuple.getStringByField(VALUE);

        // get Jedis resource and store value
        Jedis jedis = getJedisResource(database);
        // ... and store value
        jedis.set(key, value);
        // okay, now notify it to redis
        if(isNotifying) notify(jedis, key);
        pool.returnResource(jedis);
        outputCollector.ack(tuple);
    }

    @Override
    public void cleanup() {}

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {}

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return map;
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

    private void notify(Jedis jedis, String name) {
        boolean isConnectionIssue = false;
        try {
            jedis.publish(CHANNEL, name);
        } catch (JedisConnectionException e) {
            final String errMsg = "Jedis Connection error while notifying keyword [" + name + "]";
            throw new RuntimeException(errMsg, e);
        } catch (Exception e) {
            final String errMsg = "Jedis Connection error while notifying keyword  [" + name + "]";
            throw new RuntimeException(errMsg, e);
        } finally {
            if(isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
            }
        }
    }

}
