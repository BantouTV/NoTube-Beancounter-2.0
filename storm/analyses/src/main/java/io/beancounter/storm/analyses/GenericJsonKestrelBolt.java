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
public class GenericJsonKestrelBolt extends BaseRichBolt {

    private String address;

    private int port;

    private String queue;

    private Map map;

    private TopologyContext topologyContext;

    private OutputCollector outputCollector;

    private KestrelThriftClient kestrelClient;

    private ObjectMapper mapper;

    public GenericJsonKestrelBolt(String address, int port, String queue) {
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
        Map<String, String> values = getValues(tuple);
        outputCollector.ack(tuple);
        String jsonValue;
        try {
            jsonValue = mapper.writeValueAsString(values);
        } catch (IOException e) {
            throw new RuntimeException("can't serialize to Json using [" + values + "]", e);
        }
        try {
            kestrelClient.put(queue, jsonValue, Integer.MAX_VALUE);
        } catch (TException e) {
            throw new RuntimeException("can't push [" + jsonValue + "] to Kestrel using [" + address + ":" + port + "]", e);
        }
    }

    private Map<String, String> getValues(Tuple tuple) {
        Map<String, String> result = new HashMap<String, String>();
        for(String field : tuple.getFields()) {
            result.put(field, String.valueOf(tuple.getValueByField(field)));
        }
        return result;
    }

    public void closeKestrelConnection() {
        kestrelClient.close();
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("json"));
    }
}
