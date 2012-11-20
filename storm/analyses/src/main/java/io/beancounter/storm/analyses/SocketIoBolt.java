package io.beancounter.storm.analyses;

import backtype.storm.spout.KestrelThriftClient;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class SocketIoBolt extends BaseRichBolt {

    private String address;

    private int port;

    private String mentions;

    private SocketIOServer server;

    private Map map;

    private TopologyContext topologyContext;

    private OutputCollector outputCollector;

    private ObjectMapper mapper;

    private SocketIONamespace namespace;

    public SocketIoBolt(String address, int port, String mentions) {
        this.address = address;
        this.port = port;
        this.mentions = mentions;
    }

    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.map = map;
        this.topologyContext = topologyContext;
        this.outputCollector = outputCollector;
        Configuration config = new Configuration();
        config.setHostname(this.address);
        config.setPort(this.port);
        mapper = new ObjectMapper();
        server = new SocketIOServer(config);
        namespace = server.addNamespace("/" + this.mentions);
        server.addConnectListener(new ConnectListener() {
            public void onConnect(SocketIOClient client) {
            }
        });
        server.addMessageListener(new DataListener<String>() {
            public void onData(SocketIOClient client, String message, AckRequest ackRequest) {
            }
        });
        server.start();
    }

    public void execute(Tuple tuple) {
        String keyword = tuple.getString(1);
        int mentions = Integer.valueOf(tuple.getString(2));
        String jsonValue = getJsonValue(keyword, mentions);
        BroadcastOperations operations = namespace.getBroadcastOperations();
        if(operations != null) {
            operations.sendJsonObject(jsonValue);
        }
        outputCollector.ack(tuple);
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("json"));
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

}
