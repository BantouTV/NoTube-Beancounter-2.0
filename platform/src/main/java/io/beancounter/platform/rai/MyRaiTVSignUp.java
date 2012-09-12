package io.beancounter.platform.rai;

import io.beancounter.usermanager.AtomicSignUp;

/**
 * Extends {@link io.beancounter.usermanager.AtomicSignUp} to include the
 * MyRaiTV token.
 */
public class MyRaiTVSignUp extends AtomicSignUp {

    /**
     * The MyRaiTV token for the authenticated user.
     */
    private String raiToken;

    public MyRaiTVSignUp() {}

    public String getRaiToken() {
        return raiToken;
    }

    public void setRaiToken(String raiToken) {
        this.raiToken = raiToken;
    }
}
