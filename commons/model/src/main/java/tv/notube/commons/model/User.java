package tv.notube.commons.model;

import org.joda.time.DateTime;
import tv.notube.commons.model.auth.Auth;
import tv.notube.commons.tests.annotations.Random;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Models the main <a href="http://notube.tv">NoTube</a> user
 * characteristics.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class User extends Referenceable implements Serializable {

    private static final long serialVersionUID = 324345235L;

    private String name;

    private String surname;

    private DateTime profiledAt;

    private boolean forcedProfiling;

    private Map<String, Auth> services = new HashMap<String, Auth>();

    private String password;

    private String username;

    public User() {
        super();
    }

    @Random(names = {"name", "surname", "username", "password"})
    public User(String name, String surname, String username, String password) {
        super();
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public DateTime getProfiledAt() {
        return profiledAt;
    }

    public void setProfiledAt(DateTime profiledAt) {
        this.profiledAt = profiledAt;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public boolean isForcedProfiling() {
        return forcedProfiling;
    }

    public void setForcedProfiling(boolean forcedProfiling) {
        this.forcedProfiling = forcedProfiling;
    }

    public List<String> getServices() {
        return new ArrayList<String>(services.keySet());
    }

    public Auth getAuth(String service) {
        return services.get(service);
    }

    public void addService(String service, Auth auth) {
        services.put(service, auth);
    }

    public void removeService(String service) {
        services.remove(service);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", profiledAt=" + profiledAt +
                ", forcedProfiling=" + forcedProfiling +
                ", services=" + services +
                ", password='" + password + '\'' +
                ", username='" + username + '\'' +
                "} " + super.toString();
    }

}