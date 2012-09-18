package io.beancounter.commons.nlp;

import java.net.URI;

/**
 * This class models something in a text that has been recognized
 * by an {@link io.beancounter.commons.nlp.NLPEngine}.
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

        if (label != null ? !label.equals(feature.label) : feature.label != null) return false;
        if (resource != null ? !resource.equals(feature.resource) : feature.resource != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = resource != null ? resource.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Feature{" +
                "resource=" + resource +
                ", label='" + label + '\'' +
                '}';
    }
}