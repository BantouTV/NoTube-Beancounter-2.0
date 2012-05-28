package tv.notube.commons.tests;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class StringRandomiser implements Randomiser<String> {

    private String name;

    public StringRandomiser(String name) {
        this.name = name;
    }

    public Class<String> type() {
        return String.class;
    }

    public String name() {
        return name;
    }

    public String getRandom() {
        return "random-string";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StringRandomiser that = (StringRandomiser) o;

        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
