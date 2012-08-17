package io.beancounter.commons.model.auth;

import io.beancounter.commons.model.User;

/**
 * This class is used to bind an authenticated user, as they returned from
 * the {@link AuthHandler} interface, with the corresponding service
 * username or general identifier.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class AuthenticatedUser {

    private String userId;

    private User user;

    public AuthenticatedUser(String userId, User user) {
        this.userId = userId;
        this.user = user;
    }

    public String getUserId() {
        return userId;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "AuthenticatedUser{" +
                "userId='" + userId + '\'' +
                ", user=" + user +
                '}';
    }
}
