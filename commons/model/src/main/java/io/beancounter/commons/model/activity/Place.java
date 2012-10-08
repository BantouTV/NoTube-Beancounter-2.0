package io.beancounter.commons.model.activity;

import io.beancounter.commons.model.activity.uhopper.MallPlace;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Place.class, name = "PLACE"),
        @JsonSubTypes.Type(value = MallPlace.class, name = "MALL-PLACE")
})
public class Place extends Object {

    private long lat;

    private long lon;

    public Place() {
        super();
    }

    public Place(long lat, long lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public long getLat() {
        return lat;
    }

    public void setLat(long lat) {
        this.lat = lat;
    }

    public long getLon() {
        return lon;
    }

    public void setLon(long lon) {
        this.lon = lon;
    }

    @Override
    public String toString() {
        return "Place{" +
                "lat=" + lat +
                ", lon=" + lon +
                '}';
    }
}
