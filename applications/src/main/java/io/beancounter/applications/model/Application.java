package io.beancounter.applications.model;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import io.beancounter.applications.ApplicationsManager;

import java.io.Serializable;
import java.net.URL;
import java.util.UUID;

/**
 * This class models an application able to consume data from the
 * beancounter.io platform.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Application implements Serializable {

    static final long serialVersionUID = 13211239608137490L;

    private String name;

    private String description;

    private String email;

    private URL callback;

    private UUID consumerKey;

    private UUID adminKey;

    private Permissions permissions;

    @JsonCreator
    public Application(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("email") String email,
            @JsonProperty("callback") URL callback
    ) {
        this.consumerKey = UUID.randomUUID();
        this.adminKey = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.email = email;
        this.callback = callback;
        this.permissions = Permissions.buildDefault();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getEmail() {
        return email;
    }

    protected Permissions getPermissions() {
        return permissions;
    }

    protected void setPermissions(Permissions permissions) {
        this.permissions = permissions;
    }

    public void setOAuthCallback(URL callback) {
        this.callback = callback;
    }

    public URL getCallback() {
        return callback;
    }

    public UUID getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(UUID consumerKey) {
        this.consumerKey = consumerKey;
    }

    public UUID getAdminKey() {
        return adminKey;
    }

    public void setAdminKey(UUID adminKey) {
        this.adminKey = adminKey;
    }

    public boolean hasPrivileges(
            ApplicationsManager.Action action,
            ApplicationsManager.Object object
    ) {
        return permissions.hasPermission(
                action,
                object
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Application that = (Application) o;

        if (!adminKey.equals(that.adminKey)) return false;
        if (!consumerKey.equals(that.consumerKey)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = consumerKey.hashCode();
        result = 31 * result + adminKey.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Application{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", email='" + email + '\'' +
                ", callback=" + callback +
                ", consumerKey=" + consumerKey +
                ", adminKey=" + adminKey +
                ", permissions=" + permissions +
                '}';
    }

    public static Application build(String name, String description, String email, URL callback) {
        return new Application(name, description, email, callback);
    }

}
