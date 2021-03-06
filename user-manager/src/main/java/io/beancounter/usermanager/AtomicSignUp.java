package io.beancounter.usermanager;

import java.util.UUID;

/**
 * This class wraps all the needed information returned by
 * the <i>REST</i> API when a user logs in with a {@link io.beancounter.commons.model.Service}, instead
 * of using a native <i>beancounter.io</i> user.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class AtomicSignUp {

    /**
     * the <i>beancounter.io</i> user identifier
     */
    private UUID userId;

    /**
     * the <i>beancounter.io</i> username
     */
    private String username;

    /**
     * <code>true</code> if it's not a signup
     */
    private boolean returning = false;

    private String service;

    /**
     * The user identifier on the specified <i>service</i>
     */
    private String identifier;

    /**
     * The user token assigned to the user to be used for REST API requests.
     */
    private UUID userToken;

    public AtomicSignUp() {}

    public AtomicSignUp(
            UUID userId,
            String username,
            boolean returning,
            String service,
            String identifier,
            UUID userToken) {
        this.userId = userId;
        this.username = username;
        this.returning = returning;
        this.service = service;
        this.identifier = identifier;
        this.userToken = userToken;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public UUID getUserToken() {
        return userToken;
    }

    public void setUserToken(UUID userToken) {
        this.userToken = userToken;
    }

    @Override
    public String toString() {
        return "AtomicSignUp{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", returning=" + returning +
                ", service='" + service + '\'' +
                ", identifier='" + identifier + '\'' +
                ", userToken='" + userToken + '\'' +
                '}';
    }
}
