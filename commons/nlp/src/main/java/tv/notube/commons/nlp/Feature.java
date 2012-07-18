package tv.notube.commons.nlp;

import java.net.URI;

/**
 * This class models something in a text that has been recognized
 * by an {@link tv.notube.commons.nlp.NLPEngine}.
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public abstract class Feature {

    private URI resource;

    private String label;

    protected Feature(URI resource, String label) {
        this.resource = resource;
        this.label = label;
    }

    public URI getResource() {
        return resource;
    }

    public void setResource(URI resource) {
        this.resource = resource;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Feature feature = (Feature) o;

        if (resource != null ? !resource.equals(feature.resource) : feature.resource != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return resource != null ? resource.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Feature{" +
                "resource=" + resource +
                ", label='" + label + '\'' +
                '}';
    }
}