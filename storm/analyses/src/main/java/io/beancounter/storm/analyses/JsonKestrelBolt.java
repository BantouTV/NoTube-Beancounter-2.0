package io.beancounter.storm.analyses;

import backtype.storm.spout.KestrelThriftClient;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import org.apache.thrift7.TException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
// TODO (high) refactor make KestrelBolt as a template and remove this temp class
public class JsonKestrelBolt extends BaseRichBolt {

    private String address;

    private int port;

    private String queue;

    private Map map;

    private TopologyContext topologyContext;

    private OutputCollector outputCollector;

    private KestrelThriftClient kestrelClient;

    private ObjectMapper mapper;

    public JsonKestrelBolt(String address, int port, String queue) {
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
        String jsonValue = tuple.getString(2);
        outputCollector.ack(tuple);
        try {
            kestrelClient.put(queue, jsonValue, Integer.MAX_VALUE);
        } catch (TException e) {
            throw new RuntimeException("can't push [" + jsonValue + "] to Kestrel using [" + address + ":" + port + "]", e);
        }
    }

    public void closeKestrelConnection() {
        kestrelClient.close();
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("json"));
    }
}
