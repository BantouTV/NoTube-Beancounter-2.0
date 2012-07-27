package tv.notube.filter.model.pattern.rai;

import tv.notube.commons.model.activity.rai.Comment;
import tv.notube.filter.model.pattern.*;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class CommentPattern extends ObjectPattern {

    public static CommentPattern ANY = new CommentPattern();

    private StringPattern onEventPattern;

    private String type;

    public CommentPattern() {
        super(new StringPattern(CommentPattern.class.getName()), URLPattern.ANY);
        onEventPattern = StringPattern.ANY.ANY;
        type = CommentPattern.class.getName();
    }

    public CommentPattern(
            StringPattern onEventPattern,
            URLPattern url
    ) {
        super(new StringPattern(CommentPattern.class.getName()), url);
        this.onEventPattern = onEventPattern;
        type = CommentPattern.class.getName();
    }

    public StringPattern getOnEventPattern() {
        return onEventPattern;
    }

    public void setOnEventPattern(StringPattern onEventPattern) {
        this.onEventPattern = onEventPattern;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean matches(tv.notube.commons.model.activity.Object object) {
        Comment that = (Comment) object;
        return this.equals(ANY) || onEventPattern.matches(that.getOnEvent());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CommentPattern that = (CommentPattern) o;

        if (onEventPattern != null ? !onEventPattern.equals(that.onEventPattern) : that.onEventPattern != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (onEventPattern != null ? onEventPattern.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CommentPattern{" +
                "onEventPattern=" + onEventPattern +
                ", type='" + type + '\'' +
                "} " + super.toString();
    }
}
