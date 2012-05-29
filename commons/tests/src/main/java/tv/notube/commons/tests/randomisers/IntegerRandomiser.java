package tv.notube.commons.tests.randomisers;

import tv.notube.commons.tests.Randomiser;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class IntegerRandomiser implements Randomiser<Integer> {

    private String name;

    private int from;

    private int to;

    public IntegerRandomiser(String name, int max) {
        this(name, 0, max);
    }

    public IntegerRandomiser(String name, int from, int to) {
        this.name = name;
        this.from = from;
        this.to = to;
    }

    public Class<Integer> type() {
        return Integer.class;
    }

    public String name() {
        return name;
    }

    public Integer getRandom() {
        return from + (int)(Math.random() * ((to - from) + 1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntegerRandomiser that = (IntegerRandomiser) o;

        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
