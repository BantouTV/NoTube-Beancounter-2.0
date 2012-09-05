package io.beancounter.usermanager;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import io.beancounter.commons.helper.PropertiesHelper;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Properties;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;

public class JedisUserTokenManagerGuiceTest {

    private Injector injector;
    private Properties redisProperties;
    private Jedis jedis;

    @BeforeMethod
    public void setUp() throws Exception {
        redisProperties = PropertiesHelper.readFromClasspath("/redis.properties");
        injector = Guice.createInjector(new UserTokenManagerModule());
    }

    @Test
    public void tokenManagerShouldBeInjectedCorrectly() throws Exception {
        UserTokenManager tokenManager = injector.getInstance(UserTokenManager.class);
        assertNotNull(tokenManager);
    }

    @Test
    public void redisDatabaseShouldBeSetCorrectly() throws Exception {
        UserTokenManager tokenManager = injector.getInstance(UserTokenManager.class);
        tokenManager.checkTokenExists(UUID.randomUUID());

        int database = Integer.parseInt(redisProperties.getProperty("redis.db.userTokens"), 10);
        verify(jedis).select(database);
    }

    private class UserTokenManagerModule extends AbstractModule {

        @Override
        protected void configure() {
            Names.bindProperties(binder(), redisProperties);

            jedis = mock(Jedis.class);
            JedisPool jedisPool = mock(JedisPool.class);
            JedisPoolFactory jedisPoolFactory = mock(JedisPoolFactory.class);
            when(jedisPoolFactory.build()).thenReturn(jedisPool);
            when(jedisPool.getResource()).thenReturn(jedis);

            bind(JedisPoolFactory.class).toInstance(jedisPoolFactory);
            bind(UserTokenManager.class).to(JedisUserTokenManager.class);
        }
    }
}
