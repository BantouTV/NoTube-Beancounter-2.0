package tv.notube.filter.model.pattern.rai;

import tv.notube.commons.model.activity.rai.TVEvent;
import tv.notube.filter.model.pattern.*;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TVEventPattern extends ObjectPattern {

    public static final TVEventPattern ANY = new TVEventPattern(
            UUIDPattern.ANY,
            URLPattern.ANY
    );

    private UUIDPattern uuidPattern;

    private String type;

    public TVEventPattern() {
        uuidPattern = UUIDPattern.ANY;
        type = TVEventPattern.class.getName();
    }

    public TVEventPattern(
            UUIDPattern uuidPattern,
            URLPattern url
    ) {
        super(new StringPattern(TVEventPattern.class.getName()), url);
        this.uuidPattern = uuidPattern;
        this.type = TVEventPattern.class.getName();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UUIDPattern getUuidPattern() {
        return uuidPattern;
    }

    public void setUuidPattern(UUIDPattern uuidPattern) {
        this.uuidPattern = uuidPattern;
    }

    @Override
    public boolean matches(tv.notube.commons.model.activity.Object object) {
        TVEvent that = (TVEvent) object;
        return this.equals(ANY) || super.matches(that) || uuidPattern.matches(that.getId());
    }

    @Override
    public String toString() {
        return "TVEventPattern{" +
                "uuidPattern=" + uuidPattern +
                ", type='" + type + '\'' +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TVEventPattern that = (TVEventPattern) o;

        if (uuidPattern != null ? !uuidPattern.equals(that.uuidPattern) : that.uuidPattern != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return uuidPattern != null ? uuidPattern.hashCode() : 0;
    }
}
