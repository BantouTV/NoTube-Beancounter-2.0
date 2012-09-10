package io.beancounter.platform.rai;

import io.beancounter.usermanager.AtomicSignUp;

import java.util.UUID;

/**
 * Extends {@link io.beancounter.usermanager.AtomicSignUp} to include the
 * MyRaiTV token.
 */
public class MyRaiTVSignUp extends AtomicSignUp {

    /**
     * The MyRaiTV token for the authenticated user.
     */
    private String token;

    public MyRaiTVSignUp() {}

    public MyRaiTVSignUp(
            UUID userId,
            String username,
            boolean returning,
            String service,
            String identifier,
            String token
    ) {
        super(userId, username, returning, service, identifier);
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
