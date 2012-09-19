package io.beancounter.platform.validation;

import io.beancounter.applications.ApplicationsManager;
import io.beancounter.applications.ApplicationsManagerException;
import io.beancounter.commons.model.User;
import io.beancounter.usermanager.UserTokenManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValidationsTest {

    private UserTokenManager tokenManager;
    private ApplicationsManager applicationsManager;

    @BeforeMethod
    public void setUp() throws Exception {
        tokenManager = mock(UserTokenManager.class);
        applicationsManager = mock(ApplicationsManager.class);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void checkingForNullShouldThrowExceptionIfParameterIsNull() throws Exception {
        Object parameter = null;
        Validations.checkNotNull(parameter);
    }

    @Test
    public void checkingForNullShouldDoNothingIfParameterIsNotNull() throws Exception {
        Object parameter = new Object();
        Validations.checkNotNull(parameter);
    }

    @Test(
            expectedExceptions = NullPointerException.class,
            expectedExceptionsMessageRegExp = "username cannot be null"
    )
    public void checkingForNullShouldAcceptOptionalErrorMessage() throws Exception {
        String usernameParameter = null;
        Validations.checkNotNull(usernameParameter, "username cannot be null");
    }

    @Test(
            expectedExceptions = NullPointerException.class,
            expectedExceptionsMessageRegExp = "null"
    )
    public void checkingForNullShouldAcceptNullAsTheErrorMessage() throws Exception {
        String usernameParameter = null;
        String errorMessage = null;
        Validations.checkNotNull(usernameParameter, errorMessage);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void checkingIfEmptyStringIsEmptyShouldThrowException() throws Exception {
        String parameter = "";
        Validations.checkNotEmpty(parameter);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void checkingIfStringWithJustWhitespaceIsEmptyShouldThrowException() throws Exception {
        String parameter = "   \t\r\n  \t";
        Validations.checkNotEmpty(parameter);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void checkingIfNullStringIsEmptyShouldThrowException() throws Exception {
        String parameter = null;
        Validations.checkNotEmpty(parameter);
    }

    @Test
    public void checkingIfStringIsEmptyShouldDoNothingIfParameterIsNotEmpty() throws Exception {
        String parameter = "not-empty";
        Validations.checkNotEmpty(parameter);
    }

    @Test(
            expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Parameter cannot be empty"
    )
    public void checkingIfStringIsEmptyShouldAcceptOptionalErrorMessage() throws Exception {
        String parameter = "";
        Validations.checkNotEmpty(parameter, "Parameter cannot be empty");
    }

    @Test(
            expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Parameter cannot be empty"
    )
    public void checkingIfStringWithJustWhitespaceIsEmptyShouldAcceptOptionalErrorMessage() throws Exception {
        String parameter = "   \t\r\n  \t";
        Validations.checkNotEmpty(parameter, "Parameter cannot be empty");
    }

    @Test(
            expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "null"
    )
    public void checkingIfStringIsEmptyShouldAcceptNullAsTheErrorMessage() throws Exception {
        String parameter = "";
        String errorMessage = null;
        Validations.checkNotEmpty(parameter, errorMessage);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void checkingIfEmptyListIsEmptyShouldThrowException() throws Exception {
        List<String> parameter = Collections.emptyList();
        Validations.checkNotEmpty(parameter);
    }

    @Test
    public void checkingIfNonEmptyListIsEmptyShouldDoNothing() throws Exception {
        List<String> parameter = Arrays.asList("hello", "world");
        Validations.checkNotEmpty(parameter);
    }

    @Test(
            expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "The list cannot be empty"
    )
    public void checkingIfEmptyListIsEmptyShouldAcceptOptionalErrorMessage() throws Exception {
        List<String> parameter = Collections.emptyList();
        Validations.checkNotEmpty(parameter, "The list cannot be empty");
    }

    @Test(
            expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "null"
    )
    public void checkingIfEmptyListIsEmptyShouldAcceptNullAsTheErrorMessage() throws Exception {
        List<String> parameter = Collections.emptyList();
        String errorMessage = null;
        Validations.checkNotEmpty(parameter, errorMessage);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void checkingIfEmptySetIsEmptyShouldThrowException() throws Exception {
        Set<String> parameter = Collections.emptySet();
        Validations.checkNotEmpty(parameter);
    }

    @Test
    public void checkingIfNonEmptySetIsEmptyShouldDoNothing() throws Exception {
        Set<String> parameter = new HashSet<String>();
        parameter.add("hello");
        Validations.checkNotEmpty(parameter);
    }

    @Test(
            expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "The set cannot be empty"
    )
    public void checkingIfEmptySetIsEmptyShouldAcceptOptionalErrorMessage() throws Exception {
        Set<String> parameter = Collections.emptySet();
        Validations.checkNotEmpty(parameter, "The set cannot be empty");
    }

    @Test(
            expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "null"
    )
    public void checkingIfEmptySetIsEmptyShouldAcceptNullAsTheErrorMessage() throws Exception {
        Set<String> parameter = Collections.emptySet();
        String errorMessage = null;
        Validations.checkNotEmpty(parameter, errorMessage);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void checkingIfNullCollectionIsEmptyShouldFail() throws Exception {
        Set<String> parameter = null;
        Validations.checkNotEmpty(parameter);
    }

    @Test(
            expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "The set cannot be empty or null"
    )
    public void checkingIfNullCollectionIsEmptyShouldAcceptOptionalErrorMessage() throws Exception {
        Set<String> parameter = null;
        Validations.checkNotEmpty(parameter, "The set cannot be empty or null");
    }

    @Test
    public void checkingIfTrueExpressionIsTrueShouldDoNothing() throws Exception {
        Validations.check(true);
    }

    @Test
    public void checkingIfTrueExpressionIsTrueShouldAcceptOptionalErrorMessage() throws Exception {
        Validations.check(true, "This will not fail");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void checkingIfFalseExpressionIsTrueShouldFail() throws Exception {
        Validations.check(false);
    }

    @Test(
            expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "This must be true"
    )
    public void checkingIfFalseExpressionIsTrueShouldAcceptOptionalErrorMessage() throws Exception {
        Validations.check(false, "This must be true");
    }

    @Test(
            expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "null"
    )
    public void checkingIfFalseExpressionIsTrueShouldAcceptNullAsTheErrorMessage() throws Exception {
        String errorMessage = null;
        Validations.check(false, errorMessage);
    }

    @Test
    public void checkingIfValidUserTokenIsValidShouldDoNothing() throws Exception {
        UUID userToken = UUID.randomUUID();
        User user = new User();
        user.setUserToken(userToken);

        when(tokenManager.checkTokenExists(userToken)).thenReturn(true);

        Validations.validateUserToken(userToken.toString(), user, tokenManager);
    }

    @Test(
            expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "User token \\[.*\\] is not valid"
    )
    public void checkingIfExpiredUserTokenIsValidShouldThrowException() throws Exception {
        UUID userToken = UUID.randomUUID();
        User user = new User();
        user.setUserToken(userToken);

        when(tokenManager.checkTokenExists(userToken)).thenReturn(false);

        Validations.validateUserToken(userToken.toString(), user, tokenManager);
    }

    @Test(
            expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "User token \\[.*\\] is not valid"
    )
    public void checkingIfWrongUserTokenIsValidShouldThrowException() throws Exception {
        UUID wrongUserToken = UUID.randomUUID();
        User user = new User();
        user.setUserToken(UUID.randomUUID());

        when(tokenManager.checkTokenExists(wrongUserToken)).thenReturn(true);

        Validations.validateUserToken(wrongUserToken.toString(), user, tokenManager);
    }

    @Test(
            expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Error validating user token \\[.*\\]"
    )
    public void checkingIfNullUserTokenIsValidShouldThrowException() throws Exception {
        String nullUserToken = null;
        User user = new User();
        user.setUserToken(UUID.randomUUID());

        Validations.validateUserToken(nullUserToken, user, tokenManager);
    }

    @Test(
            expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "User token \\[.*\\] is not valid"
    )
    public void checkingIfUserTokenIsValidForUserWithNoUserTokenShouldThrowException() throws Exception {
        UUID userToken = UUID.randomUUID();
        User user = new User();

        Validations.validateUserToken(userToken.toString(), user, tokenManager);
    }

    @Test(
            expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Error validating user token \\[.*\\]"
    )
    public void checkingIfMalformedUserTokenIsValidShouldThrowException() throws Exception {
        String userToken = "not-a-correct-user-token";
        User user = new User();

        Validations.validateUserToken(userToken, user, tokenManager);
    }

    @Test
    public void checkingIfValidApiKeyHasCorrectPermissionsShouldDoNothing() throws Exception {
        UUID apiKey = UUID.randomUUID();
        ApplicationsManager.Action action = ApplicationsManager.Action.CREATE;
        ApplicationsManager.Object object = ApplicationsManager.Object.USER;

        when(applicationsManager.isAuthorized(apiKey, action, object)).thenReturn(true);

        Validations.validateApiKey(apiKey.toString(), applicationsManager, action, object);
    }

    @Test(
            expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Your apikey is not well formed"
    )
    public void checkingIfMalformedApiKeyHasCorrectPermissionsShouldFail() throws Exception {
        String apiKey = "not-an-api-key";

        Validations.validateApiKey(apiKey, applicationsManager, ApplicationsManager.Action.CREATE, ApplicationsManager.Object.USER);
    }

    @Test(
            expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Your apikey is not well formed"
    )
    public void checkingIfNullApiKeyHasCorrectPermissionsShouldFail() throws Exception {
        String apiKey = null;

        Validations.validateApiKey(apiKey, applicationsManager, ApplicationsManager.Action.CREATE, ApplicationsManager.Object.USER);
    }

    @Test(
            expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Error while authorizing your application"
    )
    public void givenErrorOccursWhenCheckingIfInvalidApiKeyHasCorrectPermissionsThenFail() throws Exception {
        UUID apiKey = UUID.randomUUID();
        ApplicationsManager.Action action = ApplicationsManager.Action.CREATE;
        ApplicationsManager.Object object = ApplicationsManager.Object.USER;

        when(applicationsManager.isAuthorized(apiKey, action, object))
                .thenThrow(new ApplicationsManagerException("error"));

        Validations.validateApiKey(apiKey.toString(), applicationsManager, action, object);
    }

    @Test(
            expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Application with key \\[.*\\] is not authorized"
    )
    public void checkingIfUnauthorizedApiKeyHasPermissionsShouldFail() throws Exception {
        UUID apiKey = UUID.randomUUID();
        ApplicationsManager.Action action = ApplicationsManager.Action.CREATE;
        ApplicationsManager.Object object = ApplicationsManager.Object.USER;

        when(applicationsManager.isAuthorized(apiKey, action, object)).thenReturn(false);

        Validations.validateApiKey(apiKey.toString(), applicationsManager, action, object);
    }
}
