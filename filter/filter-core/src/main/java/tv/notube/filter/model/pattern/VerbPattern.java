package tv.notube.filter.model.pattern;

import tv.notube.commons.model.activity.Verb;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class VerbPattern implements Pattern<Verb> {

    public final static VerbPattern ANY =  new VerbPattern("ANY");

    private String verb;

    public VerbPattern() {
        this("ANY");
    }

    private VerbPattern(String verb) {
        this.verb = verb;
    }

    public VerbPattern(Verb verb) {
        this.verb = verb.toString();
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VerbPattern that = (VerbPattern) o;

        if (verb != null ? !verb.equals(that.verb) : that.verb != null)
            return false;

        return true;
    }

    @Override
    public boolean matches(Verb verb) {
        return this.verb.equals(verb.toString()) || this.equals(ANY);
    }

    @Override
    public int hashCode() {
        return verb != null ? verb.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "VerbPattern{" +
                "verb='" + verb + '\'' +
                '}';
    }
}
