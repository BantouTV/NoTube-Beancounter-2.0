package tv.notube.usermanager;

/**
 * This class wraps all the needed information returned by
 * the <i>REST</i> API when a user logs in with a {@link Service}, instead
 * of using a native <i>beancounter.io</i> user.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class AtomicSignUp {

    private String username;

    private boolean returning = false;

    private String service;

    public AtomicSignUp() {}

    public AtomicSignUp(String username, boolean returning, String service) {
        this.username = username;
        this.returning = returning;
        this.service = service;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isReturning() {
        return returning;
    }

    public void setReturning(boolean returning) {
        this.returning = returning;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    @Override
    public String toString() {
        return "AtomicSignUp{" +
                "username='" + username + '\'' +
                ", returning=" + returning +
                ", service='" + service + '\'' +
                '}';
    }
}
