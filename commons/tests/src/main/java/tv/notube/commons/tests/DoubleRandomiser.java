package tv.notube.commons.tests;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class DoubleRandomiser extends DefaultRandomiser<Double> {

    private double from;

    private double to;

    public DoubleRandomiser(String name, int max) {
        super(name);
        this.to = max;
    }

    public DoubleRandomiser(String name, double from, double to) {
        super(name);
        this.from = from;
        this.to = to;
    }

    public Class<Double> type() {
        return Double.class;
    }

    public Double getRandom() {
        return from + (Math.random() * ((to - from) + 1));
    }


}
