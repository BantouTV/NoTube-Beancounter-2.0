package io.beancounter.commons.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A topic is a generic class that could be potentially interesting for a <i>beancounter.io</i>
 * {@link User}.
 *
 * @see {@link Category}
 * @see {@link Interest}
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public abstract class Topic<T extends Topic> implements Comparable<Topic>, Mergeable<T> {

    private URI resource;

    private String label;

    private double weight;

    protected Topic() {}

    protected Topic(URI resource, String label) {
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

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public abstract T merge(T nu, T old, int threshold);

    @Override
    public int compareTo(Topic that) {
        return Double.valueOf(that.getWeight()).compareTo(Double.valueOf(weight));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Topic topic = (Topic) o;

        if (resource != null ? !resource.equals(topic.resource) : topic.resource != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return resource != null ? resource.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "resource=" + resource +
                ", label='" + label + '\'' +
                ", weight=" + weight +
                '}';
    }
}
