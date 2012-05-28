package tv.notube.commons.model.auth;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class OAuthAuth extends Auth {

    private String secret;

    public OAuthAuth(String session, String secret) {
        super(session);
        this.secret = secret;
    }

    public String getSecret() {
        return secret;
    }

    @Override
    public String toString() {
        return "OAuthAuth{" +
                "secret='" + secret + '\'' +
                "} " + super.toString();
    }
}
