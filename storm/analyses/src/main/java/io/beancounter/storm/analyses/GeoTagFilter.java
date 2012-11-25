package io.beancounter.storm.analyses;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.Coordinates;
import io.beancounter.commons.model.activity.Tweet;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * Drops any Tweets which do not contain geo-location data. Additionally filters
 * out Tweets which do contain location data, but are not coming from the
 * specified country.
 *
 * For Tweets with location data and in the specified country, it will emit a
 * tuple containing:
 *      [ lat:double, long:double, text:string ]
 *
 * @author Alex Cowell
 */
public class GeoTagFilter extends BaseRichBolt {

    private static final String COUNTRY_CODE = "countryCode";

    private final String countryCode;
    private final Client client;
    private final ObjectMapper mapper;

    private OutputCollector collector;

    public GeoTagFilter(String countryCode) {
        client = Client.create();
        mapper = new ObjectMapper();
        this.countryCode = countryCode;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple tuple) {
        String tweetJson = tuple.getString(0);
        collector.ack(tuple);

        Tweet tweet;
        try {
            tweet = (Tweet) mapper.readValue(tweetJson, Activity.class).getObject();
        } catch (Exception ex) {
            return;
        }

        Coordinates coordinates = tweet.getGeo();
        if (coordinates != null && isInCountry(coordinates)) {
            collector.emit(new Values(coordinates.getLat(), coordinates.getLon(), tweet.getText()));
        }
    }

    boolean isInCountry(Coordinates coordinates) {
        WebResource resource = client.resource("http://ws.geonames.org/countryCode");
        String geoNamesResponse = resource
                .queryParam("lat", String.valueOf(coordinates.getLat()))
                .queryParam("lng", String.valueOf(coordinates.getLon()))
                .queryParam("type", "json")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
        JSONObject json = (JSONObject) JSONValue.parse(geoNamesResponse);

        return json.containsKey(COUNTRY_CODE) && countryCode.equals(json.get(COUNTRY_CODE));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("lat", "long", "text"));
    }
}
