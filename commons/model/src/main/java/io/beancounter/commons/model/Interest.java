package io.beancounter.commons.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * It represents a {@link User} interest.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Interest extends Topic<Interest> {

    private boolean visible;

    private Collection<UUID> activitiesUUIDs = new ArrayList<UUID>();

    private int activitiesSize;

    public Interest() {}

    public Interest(String label, URI resource) {
        super(resource, label);
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

    public int getActivitiesSize() {
        return this.activitiesUUIDs.size();
    }

    public void setActivitiesSize(int activitiesSize) {
        this.activitiesSize = activitiesUUIDs.size();
    }

    @Override
    public String toString() {
        return "Interest{" +
                "visible=" + visible +
                ", activitiesUUIDs=" + activitiesUUIDs +
                ", activitiesSize=" + activitiesSize +
                "} " + super.toString();
    }

    @Override
    public Interest merge(Interest nu, Interest old, int threshold) {
        // if the activities of the old one are above the threshold, drop the exceeding ones
        if(old.getActivities().size() > threshold) {
            List<UUID> oldActivities = new ArrayList<UUID>(old.getActivities());
            for(int i = 0; i < oldActivities.size() - threshold; i++) {
                oldActivities.remove(i);
            }
            old.setActivities(oldActivities);
        }
        for(UUID activityId : nu.getActivities()) {
            old.addActivity(activityId);
        }
        old.setWeight(old.getWeight() + nu.getWeight());
        return old;
    }
}
