package io.beancounter.storm.analyses;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class InstantValue {

    private long ts;

    private double tps;

    public InstantValue(long ts, double tps) {
        this.ts = ts;
        this.tps = tps;
    }

    public long getTs() {
        return ts;
    }

    public double getTps() {
        return tps;
    }
}
