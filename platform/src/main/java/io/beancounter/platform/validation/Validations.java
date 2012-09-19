package io.beancounter.platform.validation;

import io.beancounter.applications.ApplicationsManager;
import io.beancounter.applications.ApplicationsManagerException;
import io.beancounter.commons.model.User;
import io.beancounter.usermanager.UserTokenManager;

import java.util.Collection;
import java.util.UUID;

/**
 * A utility class to perform various validations on parameters to REST API
 * calls.
 */
public class Validations {

    private Validations() {}

    public static void checkNotNull(Object object) {
        if (object == null) {
            throw new NullPointerException();
        }
    }

    public static void checkNotNull(Object object, String errorMessage) {
        if (object == null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        }
    }

    public static void checkNotEmpty(String parameter) {
        if (parameter == null || parameter.trim().isEmpty()) {
            throw new IllegalArgumentException();
        }
    }

    public static void checkNotEmpty(String parameter, String errorMessage) {
        if (parameter == null || parameter.trim().isEmpty()) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    public static void checkNotEmpty(Collection parameter) {
        if (parameter == null || parameter.isEmpty()) {
            throw new IllegalArgumentException();
        }
    }

    public static void checkNotEmpty(Collection parameter, String errorMessage) {
        if (parameter == null || parameter.isEmpty()) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    public static void check(boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    public static void check(boolean expression, String errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    public static void validateUserToken(String userToken, User user, UserTokenManager tokenManager) {
        boolean tokenIsValid = true;

        try {
            UUID token = UUID.fromString(userToken);
            if (!token.equals(user.getUserToken()) || !tokenManager.checkTokenExists(token)) {
                tokenIsValid = false;
                throw new IllegalArgumentException();
            }
        } catch (Exception ex) {
            if (tokenIsValid) {
                throw new IllegalArgumentException("Error validating user token [" + userToken + "]");
            } else {
                throw new IllegalArgumentException("User token [" + userToken + "] is not valid");
            }
        }
    }

    public static void validateApiKey(
            String apiKey,
            ApplicationsManager applicationsManager,
            ApplicationsManager.Action action,
            ApplicationsManager.Object object
    ) {
        UUID applicationKey;
        try {
            applicationKey = UUID.fromString(apiKey);
        } catch (Exception e) {
            throw new IllegalArgumentException("Your apikey is not well formed");
        }

        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(applicationKey, action, object);
        } catch (ApplicationsManagerException e) {
            throw new IllegalArgumentException("Error while authorizing your application");
        }

        if (!isAuth) {
            throw new IllegalArgumentException("Application with key [" + apiKey + "] is not authorized");
        }
    }
}
