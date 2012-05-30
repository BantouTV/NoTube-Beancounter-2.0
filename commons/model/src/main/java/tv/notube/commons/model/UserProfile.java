package tv.notube.commons.model;

import tv.notube.commons.tests.annotations.*;

import java.util.*;

/**
 * It's the main class representative of a {@link User} profile.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class UserProfile extends Referenceable {

    public enum Visibility {
        PRIVATE,
        PROTECTED,
        PUBLIC
    }

    private Visibility visibility;

    private String username;

    private Collection<Type> types = new ArrayList<Type>();

    private Set<Interest> interests = new HashSet<Interest>();

    public UserProfile() {}

    @tv.notube.commons.tests.annotations.Random(names = { "username"} )
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

    public Collection<Type> getTypes() {
        return types;
    }

    public void setTypes(Collection<Type> types) {
        this.types = types;
    }

    public void setInterests(Set<Interest> interests) {
        this.interests = interests;
    }

    public Set<Interest> getInterests() {
        return interests;
    }

    public void addType(Type type) {
        this.types.add(type);
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "visibility=" + visibility +
                ", username='" + username + '\'' +
                ", types=" + types +
                ", interests=" + interests +
                "} " + super.toString();
    }
}
