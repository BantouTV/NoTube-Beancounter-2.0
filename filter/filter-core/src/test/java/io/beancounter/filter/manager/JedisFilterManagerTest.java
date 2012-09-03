package io.beancounter.filter.manager;

import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import io.beancounter.filter.model.Filter;
import io.beancounter.filter.model.pattern.ActivityPattern;
import org.codehaus.jackson.map.ObjectMapper;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class JedisFilterManagerTest {

    private FilterManager filterManager;
    private Jedis jedis;
    private JedisPool jedisPool;
    private ObjectMapper mapper;

    @BeforeMethod
    public void setUp() throws Exception {
        jedis = mock(Jedis.class);
        jedisPool = mock(JedisPool.class);
        JedisPoolFactory jedisPoolFactory = mock(JedisPoolFactory.class);
        when(jedisPoolFactory.build()).thenReturn(jedisPool);
        when(jedisPool.getResource()).thenReturn(jedis);

        filterManager = new JedisFilterManager(jedisPoolFactory);
        mapper = new ObjectMapper();
    }

    @Test(
            expectedExceptions = FilterManagerException.class,
            expectedExceptionsMessageRegExp = "Filter \\[name\\] already exists"
    )
    public void registeringAFilterWithANameWhichAlreadyExistsShouldThrowException() throws Exception {
        String name = "name";
        String description = "description";
        Set<String> queues = new HashSet<String>();
        ActivityPattern pattern = ActivityPattern.ANY;
        Filter filter = new Filter(name, description, pattern, queues);
        String filterJson = mapper.writeValueAsString(filter);

        when(jedis.get(name)).thenReturn(filterJson);

        filterManager.register(name, description, queues, pattern);
    }

    @Test
    public void validNewFilterIsSuccessfullyRegistered() throws Exception {
        String name = "name";
        String description = "description";
        ActivityPattern pattern = ActivityPattern.ANY;
        Set<String> queues = new HashSet<String>();
        queues.add("queue1");
        queues.add("queue2");

        ArgumentCaptor<String> filterNameArgument = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> filterJsonArgument = ArgumentCaptor.forClass(String.class);
        when(jedis.get(name)).thenReturn(null);
        when(jedis.set(filterNameArgument.capture(), filterJsonArgument.capture())).thenReturn("OK");

        String registeredName = filterManager.register(name, description, queues, pattern);
        Filter storedFilter = mapper.readValue(filterJsonArgument.getValue(), Filter.class);

        assertEquals(registeredName, name);
        assertEquals(filterNameArgument.getValue(), name);
        assertEquals(storedFilter.getName(), name);
        assertEquals(storedFilter.getDescription(), description);
        assertEquals(storedFilter.getQueues(), queues);
        assertEquals(storedFilter.getActivityPattern(), pattern);

        verify(jedisPool, times(2)).returnResource(jedis);
    }

    @Test
    public void jedisConnectionErrorWhileSavingFilterShouldReleaseResourcesAndThrowException() throws Exception {
        String name = "name";
        String description = "description";
        ActivityPattern pattern = ActivityPattern.ANY;
        Set<String> queues = new HashSet<String>();
        queues.add("queue1");
        queues.add("queue2");

        when(jedis.get(name)).thenReturn(null);
        when(jedis.set(anyString(), anyString())).thenThrow(new JedisConnectionException("uh-oh"));

        try {
            filterManager.register(name, description, queues, pattern);
        } catch (FilterManagerException fme) {
            assertEquals(fme.getMessage(), "Jedis Connection error while registering filter [" + name + "]");
        }

        verify(jedisPool).returnResource(jedis);
        verify(jedisPool).returnBrokenResource(jedis);
    }

    @Test
    public void errorWhileSavingFilterShouldReleaseResourcesAndThrowException() throws Exception {
        String name = "name";
        String description = "description";
        ActivityPattern pattern = ActivityPattern.ANY;
        Set<String> queues = new HashSet<String>();
        queues.add("queue1");
        queues.add("queue2");

        when(jedis.get(name)).thenReturn(null);
        when(jedis.set(anyString(), anyString())).thenThrow(new RuntimeException("uh-oh"));

        try {
            filterManager.register(name, description, queues, pattern);
        } catch (FilterManagerException fme) {
            assertEquals(fme.getMessage(), "Error while registering filter [" + name + "]");
        }

        verify(jedisPool, times(2)).returnResource(jedis);
    }
}
