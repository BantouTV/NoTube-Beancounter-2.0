package tv.notube.filter.model;

import org.joda.time.DateTime;
import tv.notube.filter.model.pattern.ActivityPattern;

/**
 * This class describes a Filter. A Filter is a component able to divert
 * to a specific queue an {@link tv.notube.commons.model.activity.Activity}
 * matching a certain {@link ActivityPattern}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Filter {

    private String name;

    private String description;

    private DateTime definedAt;

    private boolean active = false;

    private String queue;

    private ActivityPattern activityPattern;

    public Filter() {
        activityPattern = ActivityPattern.ANY;
    }

    public Filter(String name, String description, ActivityPattern activityPattern, String queue) {
        this.name = name;
        this.description = description;
        this.activityPattern = activityPattern;
        this.queue = queue;
        this.definedAt = DateTime.now();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ActivityPattern getActivityPattern() {
        return activityPattern;
    }

    public void setActivityPattern(ActivityPattern activityPattern) {
        this.activityPattern = activityPattern;
    }

    public DateTime getDefinedAt() {
        return definedAt;
    }

    public void setDefinedAt(DateTime definedAt) {
        this.definedAt = definedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Filter filter = (Filter) o;

        if (name != null ? !name.equals(filter.name) : filter.name != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Filter{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", definedAt=" + definedAt +
                ", active=" + active +
                ", queue='" + queue + '\'' +
                ", activityPattern=" + activityPattern +
                '}';
    }
}
