package tv.notube.commons.tests.randomisers;

import tv.notube.commons.tests.Randomiser;

import java.util.Random;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class LongRandomiser implements Randomiser<Long> {

    private String name;

    private long from;

    private long to;

    private Random random = new Random();

    public LongRandomiser(String name, long max) {
        this(name, 0, max);
    }

    public LongRandomiser(String name, long from, long to) {
        this.name = name;
        this.from = from;
        this.to = to;
    }

    public Class<Long> type() {
        return Long.class;
    }

    public String name() {
        return name;
    }

    public Long getRandom() {
        return from + (long)(random.nextDouble() * ((to - from) + 1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LongRandomiser that = (LongRandomiser) o;

        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
