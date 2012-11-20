package io.beancounter.commons.model.activity;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Coordinates<T, H> {

    private T lat;

    private H lon;

    public Coordinates(T lat, H lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public T getLat() {
        return lat;
    }

    public void setLat(T lat) {
        this.lat = lat;
    }

    public H getLon() {
        return lon;
    }

    public void setLon(H lon) {
        this.lon = lon;
    }

    @Override
    public String toString() {
        return "Coordinates{" +
                "lat=" + lat +
                ", lon=" + lon +
                '}';
    }
}
