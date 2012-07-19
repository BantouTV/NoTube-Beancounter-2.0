package tv.notube.filter.model.pattern;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import tv.notube.filter.model.pattern.rai.TVEventPattern;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TVEventPattern.class, name = "tv.notube.filter.model.pattern.rai.TVEventPattern")
})
public class ObjectPattern implements Pattern<tv.notube.commons.model.activity.Object> {

    public static final ObjectPattern ANY = new ObjectPattern(
            StringPattern.ANY,
            URLPattern.ANY
    );

    private StringPattern typePattern;

    private URLPattern url;

    public ObjectPattern() {
        this.typePattern = StringPattern.ANY;
        this.url = URLPattern.ANY;
    }

    public ObjectPattern(StringPattern typePattern, URLPattern url) {
        this.typePattern = typePattern;
        this.url = url;
    }

    public URLPattern getUrl() {
        return url;
    }

    public StringPattern getTypePattern() {
        return typePattern;
    }

    public void setTypePattern(StringPattern typePattern) {
        this.typePattern = typePattern;
    }

    public void setUrl(URLPattern url) {
        this.url = url;
    }

    @Override
    public boolean matches(tv.notube.commons.model.activity.Object object) {
        return this.equals(ANY) || (this.url.matches(object.getUrl()) && this
                .typePattern.matches(object.getClass().getName()));
    }

    @Override
    public String toString() {
        return "ObjectPattern{" +
                "type=" + typePattern +
                ", url=" + url +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObjectPattern that = (ObjectPattern) o;

        if (typePattern != null ? !typePattern.equals(that.typePattern) : that.typePattern != null)
            return false;
        if (url != null ? !url.equals(that.url) : that.url != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = typePattern != null ? typePattern.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }
}
