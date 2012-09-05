package io.beancounter.usermanager;

import io.beancounter.commons.helper.PropertiesHelper;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Properties;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class JedisUserTokenManagerTest {

    private UserTokenManager tokenManager;
    private Jedis jedis;
    private JedisPool jedisPool;
    private int database;

    @BeforeMethod
    public void setUp() throws Exception {
        jedis = mock(Jedis.class);
        jedisPool = mock(JedisPool.class);
        JedisPoolFactory jedisPoolFactory = mock(JedisPoolFactory.class);
        when(jedisPoolFactory.build()).thenReturn(jedisPool);
        when(jedisPool.getResource()).thenReturn(jedis);

        Properties properties = PropertiesHelper.readFromClasspath("/redis.properties");
        database = Integer.parseInt(properties.getProperty("redis.db.userTokens"), 10);

        tokenManager = new JedisUserTokenManager(jedisPoolFactory);
        ((JedisUserTokenManager) tokenManager).setDatabase(database);
    }

    @Test
    public void checkingIfNonExistentTokenExistsShouldReturnFalse() throws Exception {
        UUID token = UUID.randomUUID();
        when(jedis.exists(token.toString())).thenReturn(false);

        assertFalse(tokenManager.checkTokenExists(token));

        verify(jedis).select(database);
        verify(jedisPool).returnResource(jedis);
    }

    @Test
    public void checkingIfExistingTokenExistsShouldReturnTrue() throws Exception {
        UUID token = UUID.randomUUID();
        when(jedis.exists(token.toString())).thenReturn(true);

        assertTrue(tokenManager.checkTokenExists(token));

        verify(jedis).select(database);
        verify(jedisPool).returnResource(jedis);
    }

    @Test(expectedExceptions = UserManagerException.class)
    public void checkingIfNullTokenExistsShouldThrowException() throws Exception {
        UUID token = null;
        tokenManager.checkTokenExists(token);
    }

    @Test
    public void givenPoolResourceProblemWhenCheckingIfTokenExistsThenThrowException() throws Exception {
        UUID token = UUID.randomUUID();
        when(jedisPool.getResource())
                .thenThrow(new JedisConnectionException("Could not get a resource from the pool"));

        try {
            tokenManager.checkTokenExists(token);
        } catch (UserManagerException expected) {}

        verify(jedis, never()).select(database);
        verify(jedisPool, never()).returnResource(jedis);
        verify(jedisPool, never()).returnBrokenResource(jedis);
    }

    @Test
    public void givenJedisConnectionProblemWhileSelectingDatabaseWhenCheckingIfTokenExistsThenThrowException() throws Exception {
        UUID token = UUID.randomUUID();
        when(jedis.select(database)).thenThrow(new JedisConnectionException("error"));

        try {
            tokenManager.checkTokenExists(token);
        } catch (UserManagerException expected) {}

        verify(jedis).select(database);
        verify(jedisPool).returnBrokenResource(jedis);
    }

    @Test
    public void givenJedisConnectionProblemWhenCheckingIfTokenExistsThenThrowException() throws Exception {
        UUID token = UUID.randomUUID();
        when(jedis.exists(token.toString())).thenThrow(new JedisConnectionException("error"));

        try {
            tokenManager.checkTokenExists(token);
        } catch (UserManagerException expected) {}

        verify(jedis).select(database);
        verify(jedisPool).returnBrokenResource(jedis);
    }

    @Test
    public void givenSomeOtherProblemWhenCheckingIfTokenExistsThenThrowException() throws Exception {
        UUID token = UUID.randomUUID();
        when(jedis.exists(token.toString())).thenThrow(new RuntimeException());

        try {
            tokenManager.checkTokenExists(token);
        } catch (UserManagerException expected) {}

        verify(jedis).select(database);
        verify(jedisPool).returnResource(jedis);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void settingDatabaseToNegativeNumberShouldThrowException() throws Exception {
        ((JedisUserTokenManager) tokenManager).setDatabase(-1);
    }

    @Test
    public void settingDatabaseToNonNegativeNumberShouldBeSuccessful() throws Exception {
        ((JedisUserTokenManager) tokenManager).setDatabase(0);
        ((JedisUserTokenManager) tokenManager).setDatabase(1);
    }

    @Test
    public void creatingUserTokenShouldStoreItInTheDatabaseThenReturnTheToken() throws Exception {
        String username = "username";
        UUID userToken = tokenManager.createUserToken(username);

        assertNotNull(userToken);
        verify(jedis).select(database);
        verify(jedis).set(userToken.toString(), username);
        verify(jedisPool).returnResource(jedis);
    }

    @Test(expectedExceptions = UserManagerException.class)
    public void creatingUserWithNullUsernameShouldThrowException() throws Exception {
        String username = null;
        tokenManager.createUserToken(username);
    }

    @Test
    public void givenPoolResourceProblemWhenCreatingUserTokenThenThrowException() throws Exception {
        String username = "username";
        when(jedisPool.getResource())
                .thenThrow(new JedisConnectionException("Could not get a resource from the pool"));

        try {
            tokenManager.createUserToken(username);
        } catch (UserManagerException expected) {}

        verify(jedis, never()).select(database);
        verify(jedisPool, never()).returnResource(jedis);
        verify(jedisPool, never()).returnBrokenResource(jedis);
    }

    @Test
    public void givenJedisConnectionProblemWhileSelectingDatabaseWhenCreatingUserTokenThenThrowException() throws Exception {
        String username = "username";
        when(jedis.select(database)).thenThrow(new JedisConnectionException("error"));

        try {
            tokenManager.createUserToken(username);
        } catch (UserManagerException expected) {}

        verify(jedis).select(database);
        verify(jedisPool).returnBrokenResource(jedis);
    }

    @Test
    public void givenJedisConnectionProblemWhenCreatingUserTokenThenThrowException() throws Exception {
        String username = "username";
        when(jedis.set(anyString(), eq(username))).thenThrow(new JedisConnectionException("error"));

        try {
            tokenManager.createUserToken(username);
        } catch (UserManagerException expected) {}

        verify(jedis).select(database);
        verify(jedisPool).returnBrokenResource(jedis);
    }

    @Test
    public void givenSomeOtherProblemWhenCreatingUserTokenThenThrowException() throws Exception {
        String username = "username";
        when(jedis.set(anyString(), eq(username))).thenThrow(new RuntimeException());

        try {
            tokenManager.createUserToken(username);
        } catch (UserManagerException expected) {}

        verify(jedis).select(database);
        verify(jedisPool).returnResource(jedis);
    }

    @Test
    public void deletingNonExistentUserTokenShouldReturnFalse() throws Exception {
        UUID token = UUID.randomUUID();
        when(jedis.del(token.toString())).thenReturn(0L);

        assertFalse(tokenManager.deleteUserToken(token));
        verify(jedis).select(database);
        verify(jedisPool).returnResource(jedis);
    }

    @Test
    public void deletingExistingUserTokenShouldRemoveTheTokenAndReturnTrue() throws Exception {
        UUID token = UUID.randomUUID();
        when(jedis.del(token.toString())).thenReturn(1L);

        assertTrue(tokenManager.deleteUserToken(token));
        verify(jedis).select(database);
        verify(jedisPool).returnResource(jedis);
    }

    @Test(expectedExceptions = UserManagerException.class)
    public void deletingNullUserTokenShouldThrowException() throws Exception {
        UUID token = null;
        tokenManager.deleteUserToken(token);
    }

    @Test
    public void givenPoolResourceProblemWhenDeletingUserTokenThenThrowException() throws Exception {
        UUID token = UUID.randomUUID();
        when(jedisPool.getResource())
                .thenThrow(new JedisConnectionException("Could not get a resource from the pool"));

        try {
            tokenManager.deleteUserToken(token);
        } catch (UserManagerException expected) {}

        verify(jedis, never()).select(database);
        verify(jedisPool, never()).returnResource(jedis);
        verify(jedisPool, never()).returnBrokenResource(jedis);
    }

    @Test
    public void givenJedisConnectionProblemWhileSelectingDatabaseWhenDeletingUserTokenThenThrowException() throws Exception {
        UUID token = UUID.randomUUID();
        when(jedis.select(database)).thenThrow(new JedisConnectionException("error"));

        try {
            tokenManager.deleteUserToken(token);
        } catch (UserManagerException expected) {}

        verify(jedis).select(database);
        verify(jedisPool).returnBrokenResource(jedis);
    }

    @Test
    public void givenJedisConnectionProblemWhenDeletingUserTokenThenThrowException() throws Exception {
        UUID token = UUID.randomUUID();
        when(jedis.del(token.toString())).thenThrow(new JedisConnectionException("error"));

        try {
            tokenManager.deleteUserToken(token);
        } catch (UserManagerException expected) {}

        verify(jedis).select(database);
        verify(jedisPool).returnBrokenResource(jedis);
    }

    @Test
    public void givenSomeOtherProblemWhenDeletingUserTokenThenThrowException() throws Exception {
        UUID token = UUID.randomUUID();
        when(jedis.del(token.toString())).thenThrow(new RuntimeException());

        try {
            tokenManager.deleteUserToken(token);
        } catch (UserManagerException expected) {}

        verify(jedis).select(database);
        verify(jedisPool).returnResource(jedis);
    }
}
