package io.beancounter.commons.model.activity;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Place extends Object {

    private long lat;

    private long lon;

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
