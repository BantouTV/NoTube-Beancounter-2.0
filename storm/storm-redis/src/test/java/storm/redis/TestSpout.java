package storm.redis;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

import java.util.Map;
import java.util.Random;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TestSpout implements IRichSpout {

	SpoutOutputCollector _collector;

	Random _rand;

	public boolean isDistributed() {
		return true;
	}

	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		_collector = collector;
		_rand = new Random();
	}

	public void nextTuple() {
        Utils.sleep(1000);
		_collector.emit(new Values(
                10,
                "test-key",
                "{ \"field\": \"a first test value\", \"field2\": \"a second test value\" }")
        );
	}


	public void close() {}


	public void ack(Object id) {}

	public void fail(Object id) {}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields(RedisBolt.DATABASE, RedisBolt.KEY, RedisBolt.VALUE));
	}

	public void activate() {}

	public void deactivate() {}

	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

}
