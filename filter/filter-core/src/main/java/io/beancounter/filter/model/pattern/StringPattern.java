package io.beancounter.filter.model.pattern;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class StringPattern implements Pattern<String> {

    public static final StringPattern ANY = new StringPattern();

    private String string;

    private StringPattern() {
        string = "";
    }

    public StringPattern(String string) {
        this.string = string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }

    @Override
    public boolean matches(String s) {
        return string.equals(s) || this.equals(ANY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StringPattern that = (StringPattern) o;

        if (string != null ? !string.equals(that.string) : that.string != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return string != null ? string.hashCode() : 0;
    }
}
