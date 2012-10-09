package io.beancounter.activities;

import io.beancounter.commons.model.activity.uhopper.MallPlace;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;
import io.beancounter.commons.model.activity.Activity;

import java.io.IOException;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class MarshalAndUnmarshalTestCase {

    private static final String JSON = "{\"id\":\"891aed77-7f8b-4a28-991f-34683a281ead\",\"verb\":\"TWEET\",\"object\":{\"type\":\"TWEET\",\"url\":\"http://twitter.com/ElliottWilson/status/220164023340118017\",\"name\":\"ElliottWilson\",\"description\":null,\"text\":\"RT @RapRadar3: RAPRADAR: New Mixtape: Theophilus London Rose Island Vol. 1 http://t.co/BynRjPJm\",\"hashTags\":[],\"urls\":[\"http://bit.ly/P5Tzc1\"]},\"context\":{\"date\":1341326168000,\"service\":\"http://sally.beancounter.io\",\"mood\":null}}";

    @Test
    public void testUnmarshal() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Activity activity = objectMapper.readValue(JSON, Activity.class);
        Assert.assertNotNull(activity);
    }

    @Test
    public void testMarshalUnmarshalMallPlaceObject() throws IOException {
        MallPlace mallPlace = new MallPlace("mall-id", "sensor-id", 32343, 4321);
        ObjectMapper objectMapper = new ObjectMapper();
        String mallPlaceJson = objectMapper.writeValueAsString(mallPlace);
        MallPlace expected = objectMapper.readValue(mallPlaceJson, MallPlace.class);
        Assert.assertEquals(mallPlace, expected);
    }

}
