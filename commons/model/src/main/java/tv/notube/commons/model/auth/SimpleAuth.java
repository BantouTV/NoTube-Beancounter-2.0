package tv.notube.commons.model.auth;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class SimpleAuth extends Auth {

    private String username;

    public SimpleAuth() {}

    public SimpleAuth(String session, String username) {
        super(session);
        this.username = username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "SimpleAuth{" +
                "username='" + username + '\'' +
                "} " + super.toString();
    }
}
