package io.beancounter.commons.model.activity.uhopper;

import io.beancounter.commons.model.activity.Place;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class MallPlace extends Place {

    private String mall;

    private String sensor;

    public MallPlace() {
        super();
    }

    public MallPlace(String mall, String sensor) {
        this.mall = mall;
        this.sensor = sensor;
    }

    public MallPlace(String mall, String sensor, long lat, long lon) {
        super(lat, lon);
        this.mall = mall;
        this.sensor = sensor;
    }

    public String getMall() {
        return mall;
    }

    public void setMall(String mall) {
        this.mall = mall;
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    @Override
    public String toString() {
        return "MallPlace{" +
                "mall='" + mall + '\'' +
                ", sensor='" + sensor + '\'' +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MallPlace mallPlace = (MallPlace) o;

        if (mall != null ? !mall.equals(mallPlace.mall) : mallPlace.mall != null)
            return false;
        if (sensor != null ? !sensor.equals(mallPlace.sensor) : mallPlace.sensor != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (mall != null ? mall.hashCode() : 0);
        result = 31 * result + (sensor != null ? sensor.hashCode() : 0);
        return result;
    }
}
