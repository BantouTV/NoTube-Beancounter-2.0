package tv.notube.commons.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * It represents a {@link User} interest.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Interest implements Comparable<Interest> {

    private URI resource;

    private boolean visible;

    private double weight;

    private Collection<UUID> activitiesUUIDs = new ArrayList<UUID>();

    public Interest() {}

    public Interest(URI resource) {
        super();
        this.resource = resource;
    }

    public void setResource(URI resource) {
        this.resource = resource;
    }

    public URI getResource() {
        return resource;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Collection<UUID> getActivities() {
        return activitiesUUIDs;
    }

    public void setActivities(Collection<UUID> activitiesUUIDs) {
        this.activitiesUUIDs = activitiesUUIDs;
    }

    public boolean addActivity(UUID activityUUID) {
        return this.activitiesUUIDs.add(activityUUID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Interest interest = (Interest) o;

        if (resource != null ? !resource.equals(interest.resource) : interest.resource != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (resource != null ? resource.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Interest{" +
                "visible=" + visible +
                ", weight=" + weight +
                ", activities=" + activitiesUUIDs +
                "} " + super.toString();
    }

    @Override
    public int compareTo(Interest that) {
        return Double.valueOf(that.weight).compareTo(Double.valueOf(weight));
    }
}
