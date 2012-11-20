package io.beancounter.storm.analyses;

import backtype.storm.tuple.Fields;

import java.util.List;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TestScheme implements backtype.storm.spout.Scheme {
    public List<Object> deserialize(byte[] bytes) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Fields getOutputFields() {
        return new Fields("json");
    }
}
