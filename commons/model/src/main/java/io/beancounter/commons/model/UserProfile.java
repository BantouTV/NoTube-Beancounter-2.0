package io.beancounter.commons.model;

import org.joda.time.DateTime;

import java.util.*;

/**
 * It's the main class representative of a {@link User} profile.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class UserProfile {

    public enum Visibility {
        PRIVATE,
        PROTECTED,
        PUBLIC
    }

    private Visibility visibility;

    private String username;

    private UUID userId;

    private Set<Interest> interests = new HashSet<Interest>();

    private DateTime lastUpdated;

    public UserProfile() {}

    public UserProfile(UUID userId) {
        this.userId = userId;
    }

    @io.beancounter.commons.tests.annotations.Random(names = { "username"} )
    public UserProfile(String username) {
        this.username = username;
        this.visibility = Visibility.PUBLIC;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setInterests(Set<Interest> interests) {
        this.interests = interests;
    }

    public Set<Interest> getInterests() {
        return interests;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public DateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(DateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserProfile that = (UserProfile) o;

        if (userId != null ? !userId.equals(that.userId) : that.userId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "visibility=" + visibility +
                ", username='" + username + '\'' +
                ", userId=" + userId +
                ", interests=" + interests +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
