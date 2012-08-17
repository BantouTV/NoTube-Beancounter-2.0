package io.beancounter.commons.model;

import io.beancounter.commons.model.activity.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class UserActivities {

    private String username;

    private List<Activity> activities = new ArrayList<Activity>();

    public UserActivities(String username, List<Activity> activities) {
        this.username = username;
        this.activities = activities;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public boolean addActivity(Activity activity) {
        return activities.add(activity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserActivities that = (UserActivities) o;

        if (username != null ? !username.equals(that.username) : that.username != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "UserActivities{" +
                "username='" + username + '\'' +
                ", activities=" + activities +
                '}';
    }
}
