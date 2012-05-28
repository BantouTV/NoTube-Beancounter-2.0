package tv.notube.commons.tests;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class IntegerRandomiser implements Randomiser<Integer> {

    private String name;

    public IntegerRandomiser(String name) {
        this.name = name;
    }

    public Class<Integer> type() {
        return Integer.class;
    }

    public String name() {
        return name;
    }

    public Integer getRandom() {
        return 5;
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
