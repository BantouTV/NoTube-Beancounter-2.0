package tv.notube.commons.tests.model;

import tv.notube.commons.tests.annotations.Random;

import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FakePoint {

    private UUID id;

    private double x;

    private double y;

    @Random(names = {"id", "x", "y"})
    public FakePoint(UUID id, Double x, Double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public UUID getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FakePoint fakePoint = (FakePoint) o;

        if (Double.compare(fakePoint.x, x) != 0) return false;
        if (Double.compare(fakePoint.y, y) != 0) return false;
        if (id != null ? !id.equals(fakePoint.id) : fakePoint.id != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id != null ? id.hashCode() : 0;
        temp = x != +0.0d ? Double.doubleToLongBits(x) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = y != +0.0d ? Double.doubleToLongBits(y) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
