package io.beancounter.commons.model.auth;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class OAuthAuth extends Auth {

    private boolean expired = false;

    private String secret;

    public OAuthAuth() {}

    public OAuthAuth(String session, String secret) {
        super(session);
        this.secret = secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getSecret() {
        return secret;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public boolean isExpired() {
        return expired;
    }

    @Override
    public String toString() {
        return "OAuthAuth{" +
                "secret='" + secret + '\'' +
                "} " + super.toString();
    }
}
