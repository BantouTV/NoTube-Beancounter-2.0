package tv.notube.commons.model;

import tv.notube.commons.model.activity.Activity;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * It represents a {@link User} interest.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Interest extends Referenceable {

    private boolean visible;

    private double weight;

    private Collection<Activity> activities = new ArrayList<Activity>();

    public void setResource(URI resource) {
        this.reference = resource;
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

    public Collection<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public boolean addActivity(Activity activity) {
        return this.activities.add(activity);
    }

    @Override
    public String toString() {
        return "Interest{" +
                "visible=" + visible +
                ", weight=" + weight +
                ", activities=" + activities +
                "} " + super.toString();
    }
}
