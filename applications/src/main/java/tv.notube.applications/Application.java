package tv.notube.applications;

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

    private UUID apiKey;

    private Permissions permissions;

    public Application(String name, String description, String email, URL callback) {
        apiKey = UUID.randomUUID();
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

    public void setOAuthCallback(URL callback) {
        this.callback = callback;
    }

    public URL getCallback() {
        return callback;
    }

    public UUID getApiKey() {
        return apiKey;
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

        if (apiKey != null ? !apiKey.equals(that.apiKey) : that.apiKey != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return apiKey != null ? apiKey.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Application{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", email='" + email + '\'' +
                ", callback=" + callback +
                ", apiKey=" + apiKey +
                ", permissions=" + permissions +
                '}';
    }

    public static Application build(String name, String description, String email, URL callback) {
        return new Application(name, description, email, callback);
    }

}
