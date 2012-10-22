package io.beancounter.usermanager;

import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import io.beancounter.commons.model.OAuthToken;
import io.beancounter.commons.model.Service;
import io.beancounter.commons.model.User;
import io.beancounter.commons.model.auth.AuthHandler;
import io.beancounter.commons.model.auth.AuthHandlerException;
import io.beancounter.commons.model.auth.AuthenticatedUser;
import io.beancounter.commons.model.auth.OAuthAuth;
import io.beancounter.resolver.Resolver;
import io.beancounter.resolver.ResolverException;
import io.beancounter.resolver.ResolverMappingNotFoundException;
import io.beancounter.usermanager.services.auth.ServiceAuthorizationManager;
import io.beancounter.usermanager.services.auth.ServiceAuthorizationManagerException;
import org.codehaus.jackson.map.ObjectMapper;
import org.mockito.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.net.URL;
import java.util.UUID;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

public class JedisUserManagerImplTest {

    private UserManager userManager;
    private Jedis jedis;
    private JedisPool jedisPool;
    private Resolver resolver;
    private ServiceAuthorizationManager authManager;
    private UserTokenManager tokenManager;
    private ObjectMapper mapper;

    @BeforeMethod
    public void setUp() throws Exception {
        jedis = mock(Jedis.class);
        jedisPool = mock(JedisPool.class);
        JedisPoolFactory jedisPoolFactory = mock(JedisPoolFactory.class);
        when(jedisPoolFactory.build()).thenReturn(jedisPool);
        when(jedisPool.getResource()).thenReturn(jedis);

        resolver = mock(Resolver.class);
        authManager = mock(ServiceAuthorizationManager.class);
        tokenManager = mock(UserTokenManager.class);

        userManager = new JedisUserManagerImpl(jedisPoolFactory, resolver, authManager, tokenManager);
        mapper = new ObjectMapper();
    }

    // Test: [delete]

    @Test
    public void deletingUserShouldAlsoDeleteTheirUserToken() throws Exception {
        String username = "username";
        UUID userToken = UUID.randomUUID();
        User user = new User("Test", "User", username, "password");
        user.setUserToken(userToken);

        userManager.deleteUser(user);

        verify(jedis).del(username);
        verify(tokenManager).deleteUserToken(userToken);
        verify(jedisPool, times(1)).returnResource(jedis);
    }

    @Test
    public void deletingUserWithNoUserTokenShouldJustDeleteTheUser() throws Exception {
        String username = "username";
        User user = new User("Test", "User", username, "password");

        userManager.deleteUser(user);

        verify(jedis).del(username);
        verify(tokenManager, never()).deleteUserToken(Matchers.<UUID>any());
        verify(jedisPool, times(1)).returnResource(jedis);
    }

    @Test(expectedExceptions = UserManagerException.class)
    public void givenErrorOccursWhenDeletingUserTokenDuringUserDeletionThenThrowException() throws Exception {
        String username = "username";
        UUID userToken = UUID.randomUUID();
        User user = new User("Test", "User", username, "password");
        user.setUserToken(userToken);

        when(tokenManager.deleteUserToken(userToken)).thenThrow(new UserManagerException("error"));

        userManager.deleteUser(user);
    }

    @Test
    public void givenJedisResourceErrorOccursWhenDeletingUserThenThrowException() throws Exception {
        User user = new User("Test", "User", "username", "password");

        when(jedisPool.getResource())
                .thenThrow(new JedisConnectionException("Could not get a resource from the pool"));

        try {
            userManager.deleteUser(user);
        } catch (UserManagerException ume) {
            assertEquals(ume.getMessage(), "Error while getting a Jedis resource");
        }

        verify(jedisPool, never()).returnResource(jedis);
        verify(jedisPool, never()).returnBrokenResource(jedis);
    }

    @Test
    public void givenJedisConnectionProblemWhenSelectingDatabaseToDeleteUserThenThrowException() throws Exception {
        User user = new User("Test", "User", "username", "password");

        when(jedis.select(anyInt())).thenThrow(new JedisConnectionException("error"));

        try {
            userManager.deleteUser(user);
        } catch (UserManagerException ume) {
            assertTrue(ume.getMessage().startsWith("Jedis Connection error while selecting database"));
        }

        verify(jedisPool).returnBrokenResource(jedis);
    }

    @Test
    public void givenSomeOtherProblemWhenSelectingDatabaseToDeleteUserThenThrowException() throws Exception {
        User user = new User("Test", "User", "username", "password");

        when(jedis.select(anyInt())).thenThrow(new RuntimeException("error"));

        try {
            userManager.deleteUser(user);
        } catch (UserManagerException ume) {
            assertTrue(ume.getMessage().startsWith("Error while selecting database"));
        }

        verify(jedisPool).returnResource(jedis);
    }

    @Test
    public void givenJedisConnectionProblemWhenDeletingUserThenThrowException() throws Exception {
        String username = "username";
        User user = new User("Test", "User", username, "password");

        when(jedis.del(username)).thenThrow(new JedisConnectionException("error"));

        try {
            userManager.deleteUser(user);
        } catch (UserManagerException ume) {
            assertEquals(ume.getMessage(), "Jedis Connection error while deleting user [" + username + "]");
        }

        verify(jedisPool).returnBrokenResource(jedis);
    }

    @Test
    public void givenSomeOtherProblemWhenDeletingUserThenThrowException() throws Exception {
        String username = "username";
        User user = new User("Test", "User", username, "password");

        when(jedis.del(username)).thenThrow(new RuntimeException("error"));

        try {
            userManager.deleteUser(user);
        } catch (UserManagerException ume) {
            assertEquals(ume.getMessage(), "Error while deleting user [" + username + "]");
        }

        verify(jedisPool).returnResource(jedis);
    }

    // Test: [oauth]

    @Test(expectedExceptions = UserManagerException.class)
    public void checkingIfAnUnsupportedServiceIsSupportedShouldThrowAnException() throws Exception {
        String serviceName = "not-supported";
        when(authManager.getService(serviceName)).thenReturn(null);

        ((JedisUserManagerImpl) userManager).checkServiceIsSupported(serviceName);
    }

    @Test(expectedExceptions = UserManagerException.class)
    public void encounteringAnErrorWhileCheckingIfAServiceIsSupportedShouldThrowAnException() throws Exception {
        String serviceName = "some-service";
        when(authManager.getService(serviceName))
                .thenThrow(new ServiceAuthorizationManagerException("error"));

        ((JedisUserManagerImpl) userManager).checkServiceIsSupported(serviceName);
    }

    @Test
    public void checkingIfASupportedServiceIsSupportedShouldCompleteWithoutError() throws Exception {
        String serviceName = "supported-service";
        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));

        ((JedisUserManagerImpl) userManager).checkServiceIsSupported(serviceName);
    }

    @Test(expectedExceptions = UserManagerException.class)
    public void gettingAuthHandlerForUnsupportedServiceShouldThrowAnException() throws Exception {
        String serviceName = "not-supported";
        when(authManager.getService(serviceName)).thenReturn(null);

        ((JedisUserManagerImpl) userManager).getAuthHandlerForService(serviceName);
    }

    @Test(expectedExceptions = UserManagerException.class)
    public void encounteringAnErrorWhileCheckingIfAServiceIsSupportedWhenGettingAnAuthHandlerShouldThrowAnException() throws Exception {
        String serviceName = "some-service";
        when(authManager.getService(serviceName))
                .thenThrow(new ServiceAuthorizationManagerException("error"));

        ((JedisUserManagerImpl) userManager).getAuthHandlerForService(serviceName);
    }

    @Test(expectedExceptions = UserManagerException.class)
    public void whenAnAuthHandlerCannotBeRetrievedThenThrowAnException() throws Exception {
        String serviceName = "supported-service";
        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenThrow(new ServiceAuthorizationManagerException("error"));

        ((JedisUserManagerImpl) userManager).getAuthHandlerForService(serviceName);
    }

    @Test
    public void gettingAuthHandlerForSupportedServiceShouldReturnTheCorrectAuthHandler() throws Exception {
        String serviceName = "supported-service";
        AuthHandler authHandler = mock(AuthHandler.class);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.getService()).thenReturn(serviceName);

        AuthHandler handler = ((JedisUserManagerImpl) userManager).getAuthHandlerForService(serviceName);
        assertEquals(handler.getService(), serviceName);
    }

    @Test(expectedExceptions = UserManagerException.class)
    public void whenAnAnonymousOAuthTokenCannotBeRetrievedThenThrowAnException() throws Exception {
        String serviceName = "supported-service";
        AuthHandler authHandler = mock(AuthHandler.class);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.getToken()).thenThrow(new AuthHandlerException("error"));

        userManager.getOAuthToken(serviceName);
    }

    @Test
    public void anAnonymousOAuthTokenShouldBeRetrievedForASupportedService() throws Exception {
        String serviceName = "supported-service";
        String authRedirectUrl = "http://my.service.com/oauth/token-12345";
        AuthHandler authHandler = mock(AuthHandler.class);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.getToken()).thenReturn(new OAuthToken(new URL(authRedirectUrl)));

        OAuthToken token = userManager.getOAuthToken(serviceName);
        assertEquals(token.getRedirectPage().toString(), authRedirectUrl);
    }

    @Test(expectedExceptions = UserManagerException.class)
    public void gettingAnonymousOAuthTokenWithCustomFinalRedirectOnSystemThatDoesNotSupportUTF8ThrowsException() throws Exception {
        String serviceName = "supported-service";
        URL finalRedirectUrl = new URL("http://final.redirect.com");
        AuthHandler authHandler = mock(AuthHandler.class);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.getToken(finalRedirectUrl)).thenThrow(new AuthHandlerException("No UTF-8"));

        userManager.getOAuthToken(serviceName, finalRedirectUrl);
    }

    @Test
    public void anAnonymousOAuthTokenShouldBeRetrievedWhenAValidFinalRedirectUrlIsSpecified() throws Exception {
        String serviceName = "supported-service";
        String authRedirectUrl = "http://my.service.com/oauth/token-12345";
        URL finalRedirectUrl = new URL("http://final.redirect.com");
        AuthHandler authHandler = mock(AuthHandler.class);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.getToken(finalRedirectUrl)).thenReturn(new OAuthToken(new URL(authRedirectUrl)));

        OAuthToken token = userManager.getOAuthToken(serviceName, finalRedirectUrl);
        assertEquals(token.getRedirectPage().toString(), authRedirectUrl);
    }

    @Test
    public void gettingOAuthTokenForUserShouldBeSuccessful() throws Exception {
        String serviceName = "supported-service";
        String username = "test-user";
        String authRedirectUrl = "http://my.service.com/oauth/token-12345";
        AuthHandler authHandler = mock(AuthHandler.class);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.getToken(username)).thenReturn(new OAuthToken(new URL(authRedirectUrl)));

        OAuthToken token = userManager.getOAuthToken(serviceName, username);
        assertEquals(token.getRedirectPage().toString(), authRedirectUrl);
    }

    @Test
    public void gettingOAuthTokenForUserWithCustomCallbackShouldBeSuccessful() throws Exception {
        String serviceName = "supported-service";
        String username = "test-user";
        URL callback = new URL("http://example.com/callback/" + username);
        String authRedirectUrl = "http://my.service.com/oauth/token-12345";
        AuthHandler authHandler = mock(AuthHandler.class);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.getToken(username, callback)).thenReturn(new OAuthToken(new URL(authRedirectUrl)));

        OAuthToken token = userManager.getOAuthToken(serviceName, username, callback);
        assertEquals(token.getRedirectPage().toString(), authRedirectUrl);
    }

    @Test
    public void storeNewUserFromTwitterOAuth() throws Exception {
        String serviceName = "twitter";
        String username = "new-user";
        String serviceUserId = "17473832";
        String token = "oauth_token";
        String verifier = "oauth_verifier";
        String accessToken = "access-token-1234";
        String accessTokenSecret = "access-token-secret";
        UUID userToken = UUID.randomUUID();

        AuthHandler authHandler = mock(AuthHandler.class);
        User user = new User("Test", "User", username, "password");
        user.addService(serviceName, new OAuthAuth(accessToken, accessTokenSecret));
        AuthenticatedUser authUser = new AuthenticatedUser(serviceUserId, user);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.auth(token, verifier)).thenReturn(authUser);
        when(authHandler.getService()).thenReturn(serviceName);
        when(tokenManager.createUserToken(username)).thenReturn(userToken);
        when(resolver.resolveUsername(serviceUserId, serviceName))
                .thenThrow(new ResolverMappingNotFoundException("Means the user doesn't exist yet."));

        AtomicSignUp atomicSignUp = userManager.storeUserFromOAuth(serviceName, token, verifier);
        user.setUserToken(userToken);

        assertEquals(atomicSignUp.getUsername(), username);
        assertEquals(atomicSignUp.getUserId(), user.getId());
        assertEquals(atomicSignUp.getService(), serviceName);
        assertEquals(atomicSignUp.getIdentifier(), serviceUserId);
        assertEquals(atomicSignUp.getUserToken(), userToken);
        assertFalse(atomicSignUp.isReturning());

        verify(authHandler).auth(token, verifier);
        verify(tokenManager).createUserToken(username);
        verify(resolver).store(serviceUserId, serviceName, user.getId(), username);
        verify(jedis).select(0);
        verify(jedis).set(username, mapper.writeValueAsString(user));
        verify(jedisPool).getResource();
        verify(jedisPool).returnResource(jedis);
    }

    @Test
    public void storeNewTwitterOAuthCredentialsForExistingUser() throws Exception {
        String serviceName = "twitter";
        String username = "existing-user";
        String serviceUserId = "17473832";
        String token = "oauth_token";
        String verifier = "oauth_verifier";
        String accessToken = "access-token-1234";
        String accessTokenSecret = "access-token-secret";
        UUID oldUserToken = UUID.randomUUID();
        UUID newUserToken = UUID.randomUUID();

        AuthHandler authHandler = mock(AuthHandler.class);
        User oldUser = new User("Test", "User", username, "password");
        oldUser.setUserToken(oldUserToken);
        User newUser = new User("Test", "User", username, "password");
        newUser.setId(oldUser.getId());
        newUser.addService(serviceName, new OAuthAuth(accessToken, accessTokenSecret));
        AuthenticatedUser authUser = new AuthenticatedUser(serviceUserId, newUser);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.auth(token, verifier)).thenReturn(authUser);
        when(authHandler.getService()).thenReturn(serviceName);
        when(tokenManager.createUserToken(username)).thenReturn(newUserToken);
        when(resolver.resolveUsername(serviceUserId, serviceName)).thenReturn(username);
        when(jedis.get(username)).thenReturn(mapper.writeValueAsString(oldUser));

        AtomicSignUp atomicSignUp = userManager.storeUserFromOAuth(serviceName, token, verifier);
        newUser.setUserToken(newUserToken);

        assertEquals(atomicSignUp.getUsername(), username);
        assertEquals(atomicSignUp.getUserId(), newUser.getId());
        assertEquals(atomicSignUp.getService(), serviceName);
        assertEquals(atomicSignUp.getIdentifier(), serviceUserId);
        assertEquals(atomicSignUp.getUserToken(), newUserToken);
        assertTrue(atomicSignUp.isReturning());

        verify(tokenManager).deleteUserToken(oldUserToken);
        verify(tokenManager).createUserToken(username);
        verify(jedis, times(2)).select(0);
        verify(jedis).get(username);
        verify(jedis).set(username, mapper.writeValueAsString(newUser));
        verify(jedisPool, times(2)).getResource();
        verify(jedisPool, times(2)).returnResource(jedis);
    }

    @Test
    public void storeNewTwitterOAuthCredentialsForExistingUserWithNullUserToken() throws Exception {
        String serviceName = "twitter";
        String username = "existing-user";
        String serviceUserId = "17473832";
        String token = "oauth_token";
        String verifier = "oauth_verifier";
        String accessToken = "access-token-1234";
        String accessTokenSecret = "access-token-secret";
        UUID oldUserToken = null;
        UUID newUserToken = UUID.randomUUID();

        AuthHandler authHandler = mock(AuthHandler.class);
        User oldUser = new User("Test", "User", username, "password");
        User newUser = new User("Test", "User", username, "password");
        newUser.setId(oldUser.getId());
        newUser.addService(serviceName, new OAuthAuth(accessToken, accessTokenSecret));
        AuthenticatedUser authUser = new AuthenticatedUser(serviceUserId, newUser);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.auth(token, verifier)).thenReturn(authUser);
        when(authHandler.getService()).thenReturn(serviceName);
        when(tokenManager.createUserToken(username)).thenReturn(newUserToken);
        when(resolver.resolveUsername(serviceUserId, serviceName)).thenReturn(username);
        when(jedis.get(username)).thenReturn(mapper.writeValueAsString(oldUser));

        AtomicSignUp atomicSignUp = userManager.storeUserFromOAuth(serviceName, token, verifier);
        newUser.setUserToken(newUserToken);

        assertEquals(atomicSignUp.getUsername(), username);
        assertEquals(atomicSignUp.getUserId(), newUser.getId());
        assertEquals(atomicSignUp.getService(), serviceName);
        assertEquals(atomicSignUp.getIdentifier(), serviceUserId);
        assertEquals(atomicSignUp.getUserToken(), newUserToken);
        assertTrue(atomicSignUp.isReturning());

        verify(tokenManager, never()).deleteUserToken(oldUserToken);
        verify(tokenManager).createUserToken(username);
        verify(jedis, times(2)).select(0);
        verify(jedis).get(username);
        verify(jedis).set(username, mapper.writeValueAsString(newUser));
        verify(jedisPool, times(2)).getResource();
        verify(jedisPool, times(2)).returnResource(jedis);
    }

    @Test
    public void storeNewUserFromFacebookOAuth() throws Exception {
        String serviceName = "facebook";
        String username = "new-user";
        String serviceUserId = "17473832";
        String token = null;
        String code = "oauth2_code";
        String accessToken = "access-token-1234";
        String accessTokenSecret = "access-token-secret";
        UUID userToken = UUID.randomUUID();

        AuthHandler authHandler = mock(AuthHandler.class);
        User user = new User("Test", "User", username, "password");
        user.addService(serviceName, new OAuthAuth(accessToken, accessTokenSecret));
        AuthenticatedUser authUser = new AuthenticatedUser(serviceUserId, user);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.auth(code)).thenReturn(authUser);
        when(authHandler.getService()).thenReturn(serviceName);
        when(tokenManager.createUserToken(username)).thenReturn(userToken);
        when(resolver.resolveUsername(serviceUserId, serviceName))
                .thenThrow(new ResolverMappingNotFoundException("Means the user doesn't exist yet."));

        AtomicSignUp atomicSignUp = userManager.storeUserFromOAuth(serviceName, token, code);
        user.setUserToken(userToken);

        assertEquals(atomicSignUp.getUsername(), username);
        assertEquals(atomicSignUp.getUserId(), user.getId());
        assertEquals(atomicSignUp.getService(), serviceName);
        assertEquals(atomicSignUp.getIdentifier(), serviceUserId);
        assertEquals(atomicSignUp.getUserToken(), userToken);
        assertFalse(atomicSignUp.isReturning());

        verify(authHandler).auth(code);
        verify(tokenManager).createUserToken(username);
        verify(resolver).store(serviceUserId, serviceName, user.getId(), username);
        verify(jedis).select(0);
        verify(jedis).set(username, mapper.writeValueAsString(user));
        verify(jedisPool).getResource();
        verify(jedisPool).returnResource(jedis);
    }

    @Test
    public void storeNewFacebookOAuthCredentialsForExistingUser() throws Exception {
        String serviceName = "facebook";
        String username = "existing-user";
        String serviceUserId = "17473832";
        String token = null;
        String code = "oauth2_code";
        String accessToken = "access-token-1234";
        String accessTokenSecret = "access-token-secret";
        UUID oldUserToken = UUID.randomUUID();
        UUID newUserToken = UUID.randomUUID();

        AuthHandler authHandler = mock(AuthHandler.class);
        User oldUser = new User("Test", "User", username, "password");
        oldUser.setUserToken(oldUserToken);
        User newUser = new User("Test", "User", username, "password");
        newUser.setId(oldUser.getId());
        newUser.addService(serviceName, new OAuthAuth(accessToken, accessTokenSecret));
        AuthenticatedUser authUser = new AuthenticatedUser(serviceUserId, newUser);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.auth(code)).thenReturn(authUser);
        when(authHandler.getService()).thenReturn(serviceName);
        when(tokenManager.createUserToken(username)).thenReturn(newUserToken);
        when(resolver.resolveUsername(serviceUserId, serviceName)).thenReturn(username);
        when(jedis.get(username)).thenReturn(mapper.writeValueAsString(oldUser));

        AtomicSignUp atomicSignUp = userManager.storeUserFromOAuth(serviceName, token, code);
        newUser.setUserToken(newUserToken);

        assertEquals(atomicSignUp.getUsername(), username);
        assertEquals(atomicSignUp.getUserId(), newUser.getId());
        assertEquals(atomicSignUp.getService(), serviceName);
        assertEquals(atomicSignUp.getIdentifier(), serviceUserId);
        assertEquals(atomicSignUp.getUserToken(), newUserToken);
        assertTrue(atomicSignUp.isReturning());

        verify(tokenManager).deleteUserToken(oldUserToken);
        verify(tokenManager).createUserToken(username);
        verify(jedis, times(2)).select(0);
        verify(jedis, times(1)).get(username);
        verify(jedis).set(username, mapper.writeValueAsString(newUser));
        verify(jedisPool, times(2)).getResource();
        verify(jedisPool, times(2)).returnResource(jedis);
    }

    @Test
    public void storeNewFacebookOAuthCredentialsForExistingUserWithNullUserToken() throws Exception {
        String serviceName = "facebook";
        String username = "existing-user";
        String serviceUserId = "17473832";
        String token = null;
        String code = "oauth2_code";
        String accessToken = "access-token-1234";
        String accessTokenSecret = "access-token-secret";
        UUID oldUserToken = null;
        UUID newUserToken = UUID.randomUUID();

        AuthHandler authHandler = mock(AuthHandler.class);
        User oldUser = new User("Test", "User", username, "password");
        User newUser = new User("Test", "User", username, "password");
        newUser.setId(oldUser.getId());
        newUser.addService(serviceName, new OAuthAuth(accessToken, accessTokenSecret));
        AuthenticatedUser authUser = new AuthenticatedUser(serviceUserId, newUser);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.auth(code)).thenReturn(authUser);
        when(authHandler.getService()).thenReturn(serviceName);
        when(tokenManager.createUserToken(username)).thenReturn(newUserToken);
        when(resolver.resolveUsername(serviceUserId, serviceName)).thenReturn(username);
        when(jedis.get(username)).thenReturn(mapper.writeValueAsString(oldUser));

        AtomicSignUp atomicSignUp = userManager.storeUserFromOAuth(serviceName, token, code);
        newUser.setUserToken(newUserToken);

        assertEquals(atomicSignUp.getUsername(), username);
        assertEquals(atomicSignUp.getUserId(), newUser.getId());
        assertEquals(atomicSignUp.getService(), serviceName);
        assertEquals(atomicSignUp.getIdentifier(), serviceUserId);
        assertEquals(atomicSignUp.getUserToken(), newUserToken);
        assertTrue(atomicSignUp.isReturning());

        verify(tokenManager, never()).deleteUserToken(oldUserToken);
        verify(tokenManager).createUserToken(username);
        verify(jedis, times(2)).select(0);
        verify(jedis).get(username);
        verify(jedis).set(username, mapper.writeValueAsString(newUser));
        verify(jedisPool, times(2)).getResource();
        verify(jedisPool, times(2)).returnResource(jedis);
    }

    @Test(expectedExceptions = UserManagerException.class)
    public void whenTwitterUserCannotBeAuthenticatedAnExceptionShouldBeThrown() throws Exception {
        String serviceName = "twitter";
        String token = "oauth_token";
        String verifier = "oauth_verifier";

        AuthHandler authHandler = mock(AuthHandler.class);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.auth(token, verifier)).thenThrow(new AuthHandlerException("error"));

        userManager.storeUserFromOAuth(serviceName, token, verifier);
    }

    @Test(expectedExceptions = UserManagerException.class)
    public void whenFacebookUserCannotBeAuthenticatedAnExceptionShouldBeThrown() throws Exception {
        String serviceName = "facebook";
        String token = null;
        String code = "oauth2_code";

        AuthHandler authHandler = mock(AuthHandler.class);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.auth(code)).thenThrow(new AuthHandlerException("error"));

        userManager.storeUserFromOAuth(serviceName, token, code);
    }

    @Test(expectedExceptions = UserManagerException.class)
    public void givenTwitterUserWhenErrorOccursWhileResolvingUsernameThenAnExceptionShouldBeThrown() throws Exception {
        String serviceName = "twitter";
        String username = "new-user";
        String serviceUserId = "17473832";
        String token = "oauth_token";
        String verifier = "oauth_verifier";

        AuthHandler authHandler = mock(AuthHandler.class);
        User user = new User("Test", "User", username, "password");
        AuthenticatedUser authUser = new AuthenticatedUser(serviceUserId, user);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.auth(token, verifier)).thenReturn(authUser);
        when(authHandler.getService()).thenReturn(serviceName);
        when(resolver.resolveUsername(serviceUserId, serviceName)).thenThrow(new ResolverException("error"));

        userManager.storeUserFromOAuth(serviceName, token, verifier);
    }

    @Test(expectedExceptions = UserManagerException.class)
    public void givenFacebookUserWhenErrorOccursWhileResolvingUsernameThenAnExceptionShouldBeThrown() throws Exception {
        String serviceName = "facebook";
        String username = "new-user";
        String serviceUserId = "17473832";
        String token = null;
        String code = "oauth2_code";

        AuthHandler authHandler = mock(AuthHandler.class);
        User user = new User("Test", "User", username, "password");
        AuthenticatedUser authUser = new AuthenticatedUser(serviceUserId, user);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.auth(code)).thenReturn(authUser);
        when(authHandler.getService()).thenReturn(serviceName);
        when(resolver.resolveUsername(serviceUserId, serviceName)).thenThrow(new ResolverException("error"));

        userManager.storeUserFromOAuth(serviceName, token, code);
    }

    @Test(expectedExceptions = UserManagerException.class)
    public void givenNewTwitterUserWhenErrorOccursWhileStoringNewResolverMappingThenAnExceptionShouldBeThrown() throws Exception {
        String serviceName = "twitter";
        String username = "new-user";
        String serviceUserId = "17473832";
        String token = "oauth_token";
        String verifier = "oauth_verifier";

        AuthHandler authHandler = mock(AuthHandler.class);
        User user = new User("Test", "User", username, "password");
        AuthenticatedUser authUser = new AuthenticatedUser(serviceUserId, user);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.auth(token, verifier)).thenReturn(authUser);
        when(authHandler.getService()).thenReturn(serviceName);
        when(resolver.resolveUsername(serviceUserId, serviceName))
                .thenThrow(new ResolverMappingNotFoundException("Means the service->user mapping doesn't exist yet."));
        doThrow(new ResolverException("error"))
                .when(resolver).store(serviceUserId, serviceName, user.getId(), user.getUsername());

        userManager.storeUserFromOAuth(serviceName, token, verifier);
    }

    @Test(expectedExceptions = UserManagerException.class)
    public void givenNewFacebookUserWhenErrorOccursWhileStoringNewResolverMappingThenAnExceptionShouldBeThrown() throws Exception {
        String serviceName = "facebook";
        String username = "new-user";
        String serviceUserId = "17473832";
        String token = null;
        String code = "oauth2_code";

        AuthHandler authHandler = mock(AuthHandler.class);
        User user = new User("Test", "User", username, "password");
        AuthenticatedUser authUser = new AuthenticatedUser(serviceUserId, user);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.auth(code)).thenReturn(authUser);
        when(authHandler.getService()).thenReturn(serviceName);
        when(resolver.resolveUsername(serviceUserId, serviceName))
                .thenThrow(new ResolverMappingNotFoundException("Means the service->user mapping doesn't exist yet."));
        doThrow(new ResolverException("error"))
                .when(resolver).store(serviceUserId, serviceName, user.getId(), user.getUsername());

        userManager.storeUserFromOAuth(serviceName, token, code);
    }

    @Test(expectedExceptions = UserManagerException.class)
    public void givenNewTwitterUserWhenErrorOccursWhileCreatingUserTokenThenThrowException() throws Exception {
        String serviceName = "twitter";
        String username = "new-user";
        String serviceUserId = "17473832";
        String token = "oauth_token";
        String verifier = "oauth_verifier";

        AuthHandler authHandler = mock(AuthHandler.class);
        User user = new User("Test", "User", username, "password");
        AuthenticatedUser authUser = new AuthenticatedUser(serviceUserId, user);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.auth(token, verifier)).thenReturn(authUser);
        when(authHandler.getService()).thenReturn(serviceName);
        when(resolver.resolveUsername(serviceUserId, serviceName))
                .thenThrow(new ResolverMappingNotFoundException("Means the service->user mapping doesn't exist yet."));
        when(tokenManager.createUserToken(username))
                .thenThrow(new UserManagerException("Error"));

        userManager.storeUserFromOAuth(serviceName, token, verifier);
    }

    @Test(expectedExceptions = UserManagerException.class)
    public void givenNewFacebookUserWhenErrorOccursWhileCreatingUserTokenThenThrowException() throws Exception {
        String serviceName = "facebook";
        String username = "new-user";
        String serviceUserId = "17473832";
        String token = null;
        String code = "oauth2_code";

        AuthHandler authHandler = mock(AuthHandler.class);
        User user = new User("Test", "User", username, "password");
        AuthenticatedUser authUser = new AuthenticatedUser(serviceUserId, user);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.auth(code)).thenReturn(authUser);
        when(authHandler.getService()).thenReturn(serviceName);
        when(resolver.resolveUsername(serviceUserId, serviceName))
                .thenThrow(new ResolverMappingNotFoundException("Means the service->user mapping doesn't exist yet."));
        when(tokenManager.createUserToken(username))
                .thenThrow(new UserManagerException("Error"));

        userManager.storeUserFromOAuth(serviceName, token, code);
    }

    @Test
    public void givenDeletedTwitterUserWhenLoggingShouldCreateNewUser() throws Exception {
        String serviceName = "twitter";
        String username = "deleted-user";
        String serviceUserId = "17473832";
        String token = "oauth_token";
        String verifier = "oauth_verifier";

        AuthHandler authHandler = mock(AuthHandler.class);
        User user = new User("Test", "User", username, "password");
        AuthenticatedUser authUser = new AuthenticatedUser(serviceUserId, user);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.auth(token, verifier)).thenReturn(authUser);
        when(authHandler.getService()).thenReturn(serviceName);
        when(resolver.resolveUsername(serviceUserId, serviceName)).thenReturn(username);
        when(jedis.get(username)).thenReturn(null);

        AtomicSignUp atomicSignUp = userManager.storeUserFromOAuth(serviceName, token, verifier);
        assertNotNull(atomicSignUp);
        assertEquals(atomicSignUp.getIdentifier(), serviceUserId);
        assertEquals(atomicSignUp.getService(), serviceName);
        assertEquals(atomicSignUp.getUsername(), username);
        assertEquals(atomicSignUp.isReturning(), false);
    }

    @Test
    public void givenDeletedFacebookUserWhenLoggingInShouldCreateNewUser() throws Exception {
        String serviceName = "facebook";
        String username = "deleted-user";
        String serviceUserId = "17473832";
        String token = null;
        String code = "oauth2_code";

        AuthHandler authHandler = mock(AuthHandler.class);
        User user = new User("Test", "User", username, "password");
        AuthenticatedUser authUser = new AuthenticatedUser(serviceUserId, user);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.auth(code)).thenReturn(authUser);
        when(authHandler.getService()).thenReturn(serviceName);
        when(resolver.resolveUsername(serviceUserId, serviceName)).thenReturn(username);
        when(jedis.get(username)).thenReturn(null);

        AtomicSignUp atomicSignUp = userManager.storeUserFromOAuth(serviceName, token, code);
        assertNotNull(atomicSignUp);
        assertEquals(atomicSignUp.getIdentifier(), serviceUserId);
        assertEquals(atomicSignUp.getService(), serviceName);
        assertEquals(atomicSignUp.getUsername(), username);
        assertEquals(atomicSignUp.isReturning(), false);
    }

    @Test
    public void registeringTwitterOAuthServiceToUserWithNoUserTokenShouldBeSuccessful() throws Exception {
        String serviceName = "twiter";
        String username = "username";
        String serviceUserId = "17473832";
        String token = "oauth_token";
        String verifier = "oauth_verifier";
        UUID userToken = UUID.randomUUID();

        AuthHandler authHandler = mock(AuthHandler.class);
        User user = new User("Test", "User", username, "password");
        AuthenticatedUser authUser = new AuthenticatedUser(serviceUserId, user);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.auth(user, token, verifier)).thenReturn(authUser);
        when(tokenManager.createUserToken(username)).thenReturn(userToken);

        userManager.registerOAuthService(serviceName, user, token, verifier);
        user.setUserToken(userToken);

        verify(authHandler).auth(user, token, verifier);
        verify(tokenManager).createUserToken(username);
        verify(tokenManager, never()).deleteUserToken(Matchers.<UUID>any());
        verify(resolver).store(serviceUserId, serviceName, user.getId(), username);
        verify(jedis).select(0);
        verify(jedis).set(username, mapper.writeValueAsString(user));
        verify(jedisPool).getResource();
        verify(jedisPool).returnResource(jedis);
    }

    @Test
    public void registeringTwitterOAuthServiceToUserWithExistingUserTokenShouldBeSuccessful() throws Exception {
        String serviceName = "twiter";
        String username = "username";
        String serviceUserId = "17473832";
        String token = "oauth_token";
        String verifier = "oauth_verifier";
        UUID oldUserToken = UUID.randomUUID();
        UUID newUserToken = UUID.randomUUID();

        AuthHandler authHandler = mock(AuthHandler.class);
        User user = new User("Test", "User", username, "password");
        user.setUserToken(oldUserToken);
        AuthenticatedUser authUser = new AuthenticatedUser(serviceUserId, user);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.auth(user, token, verifier)).thenReturn(authUser);
        when(tokenManager.createUserToken(username)).thenReturn(newUserToken);

        userManager.registerOAuthService(serviceName, user, token, verifier);
        user.setUserToken(newUserToken);

        verify(authHandler).auth(user, token, verifier);
        verify(tokenManager).deleteUserToken(oldUserToken);
        verify(tokenManager).createUserToken(username);
        verify(resolver).store(serviceUserId, serviceName, user.getId(), username);
        verify(jedis).select(0);
        verify(jedis).set(username, mapper.writeValueAsString(user));
        verify(jedisPool).getResource();
        verify(jedisPool).returnResource(jedis);
    }

    @Test
    public void registeringFacebookOAuthServiceToUserWithNoUserTokenShouldBeSuccessful() throws Exception {
        String serviceName = "facebook";
        String username = "username";
        String serviceUserId = "17473832";
        String token = null;
        String code = "oauth2_code";
        UUID userToken = UUID.randomUUID();

        AuthHandler authHandler = mock(AuthHandler.class);
        User user = new User("Test", "User", username, "password");
        AuthenticatedUser authUser = new AuthenticatedUser(serviceUserId, user);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.auth(user, token, code)).thenReturn(authUser);
        when(tokenManager.createUserToken(username)).thenReturn(userToken);

        userManager.registerOAuthService(serviceName, user, token, code);
        user.setUserToken(userToken);

        verify(authHandler).auth(user, token, code);
        verify(tokenManager).createUserToken(username);
        verify(tokenManager, never()).deleteUserToken(Matchers.<UUID>any());
        verify(resolver).store(serviceUserId, serviceName, user.getId(), username);
        verify(jedis).select(0);
        verify(jedis).set(username, mapper.writeValueAsString(user));
        verify(jedisPool).getResource();
        verify(jedisPool).returnResource(jedis);
    }

    @Test
    public void registeringFacebookOAuthServiceToUserWithExistingUserTokenShouldBeSuccessful() throws Exception {
        String serviceName = "facebook";
        String username = "username";
        String serviceUserId = "17473832";
        String token = null;
        String code = "oauth2_code";
        UUID oldUserToken = UUID.randomUUID();
        UUID newUserToken = UUID.randomUUID();

        AuthHandler authHandler = mock(AuthHandler.class);
        User user = new User("Test", "User", username, "password");
        user.setUserToken(oldUserToken);
        AuthenticatedUser authUser = new AuthenticatedUser(serviceUserId, user);

        when(authManager.getService(serviceName)).thenReturn(new Service(serviceName));
        when(authManager.getHandler(serviceName)).thenReturn(authHandler);
        when(authHandler.auth(user, token, code)).thenReturn(authUser);
        when(tokenManager.createUserToken(username)).thenReturn(newUserToken);

        userManager.registerOAuthService(serviceName, user, token, code);
        user.setUserToken(newUserToken);

        verify(authHandler).auth(user, token, code);
        verify(tokenManager).deleteUserToken(oldUserToken);
        verify(tokenManager).createUserToken(username);
        verify(resolver).store(serviceUserId, serviceName, user.getId(), username);
        verify(jedis).select(0);
        verify(jedis).set(username, mapper.writeValueAsString(user));
        verify(jedisPool).getResource();
        verify(jedisPool).returnResource(jedis);
    }
}
