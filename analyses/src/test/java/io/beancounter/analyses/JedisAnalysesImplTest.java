package io.beancounter.analyses;

import io.beancounter.commons.helper.PropertiesHelper;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import io.beancounter.commons.model.AnalysisResult;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class JedisAnalysesImplTest {

    private Analyses analyses;
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

        mapper = spy(new ObjectMapper());
        analyses = new JedisAnalysesImpl(jedisPoolFactory, mapper);
    }

    @Test
    public void analysesDatabaseIsConfiguredInRedisProperties() throws Exception {
        Properties properties = PropertiesHelper.readFromClasspath("/redis.properties");
        int database = Integer.parseInt(properties.getProperty("redis.db.analyses"), 10);
        assertNotNull(database);
        assertEquals(8, database);
    }

    @Test
    public void storeNewAnalysisResult() throws Exception {
        UUID analysisId = UUID.randomUUID();
        AnalysisResult analysisResult = new AnalysisResult(analysisId);

        analyses.store(analysisResult);

        verify(jedisPool).getResource();
        verify(jedisPool).returnResource(jedis);
        verify(jedis).set(analysisId.toString(), mapper.writeValueAsString(analysisResult));
    }

    @Test
    public void givenJsonMappingErrorWhenStoringAnalysisResultThenThrowException() throws Exception {
        UUID analysisId = UUID.randomUUID();
        AnalysisResult analysisResult = new AnalysisResult(analysisId);

        when(mapper.writeValueAsString(analysisResult)).thenThrow(new IOException());

        try {
            analyses.store(analysisResult);
            fail();
        } catch (AnalysesException expected) {
            verify(jedisPool, never()).getResource();
            verify(jedisPool, never()).returnResource(jedis);
        }
    }

    @Test
    public void givenJedisConnectionProblemWhenStoringAnalysisResultThenThrowException() throws Exception {
        UUID analysisId = UUID.randomUUID();
        AnalysisResult analysisResult = new AnalysisResult(analysisId);

        when(jedis.set(analysisId.toString(), mapper.writeValueAsString(analysisResult)))
                .thenThrow(new JedisConnectionException("error"));

        try {
            analyses.store(analysisResult);
            fail();
        } catch (AnalysesException expected) {
            verify(jedisPool).getResource();
            verify(jedisPool).returnBrokenResource(jedis);
        }
    }

    @Test
    public void givenSomeOtherProblemWhenStoringAnalysisResultThenThrowException() throws Exception {
        UUID analysisId = UUID.randomUUID();
        AnalysisResult analysisResult = new AnalysisResult(analysisId);

        when(jedis.set(analysisId.toString(), mapper.writeValueAsString(analysisResult)))
                .thenThrow(new RuntimeException());

        try {
            analyses.store(analysisResult);
            fail();
        } catch (AnalysesException expected) {
            verify(jedisPool).getResource();
            verify(jedisPool).returnResource(jedis);
        }
    }

    @Test
    public void lookupExistingAnalysisResultShouldReturnCorrectAnalysisResult() throws Exception {
        UUID analysisId = UUID.randomUUID();
        AnalysisResult expectedResult = new AnalysisResult(analysisId);
        String expectedResultJson = mapper.writeValueAsString(expectedResult);

        when(jedis.get(analysisId.toString())).thenReturn(expectedResultJson);

        AnalysisResult analysisResult = analyses.lookup(analysisId);
        assertNotNull(analysisResult);
        assertEquals(analysisResult, expectedResult);

        verify(jedisPool).getResource();
        verify(jedisPool).returnResource(jedis);
    }

    @Test
    public void lookupNonExistentAnalysisResultShouldReturnNull() throws Exception {
        UUID analysisId = UUID.randomUUID();

        when(jedis.get(analysisId.toString())).thenReturn(null);

        AnalysisResult analysisResult = analyses.lookup(analysisId);
        assertNull(analysisResult);

        verify(jedisPool).getResource();
        verify(jedisPool).returnResource(jedis);
    }

    @Test
    public void givenJsonMappingErrorWhenLookingUpAnalysisResultThenThrowException() throws Exception {
        UUID analysisId = UUID.randomUUID();
        AnalysisResult analysisResult = new AnalysisResult(analysisId);
        String resultJson = mapper.writeValueAsString(analysisResult);

        when(jedis.get(analysisId.toString())).thenReturn(resultJson);
        when(mapper.readValue(resultJson, AnalysisResult.class)).thenThrow(new IOException());

        try {
            analyses.lookup(analysisId);
            fail();
        } catch (AnalysesException expected) {
            verify(jedisPool).getResource();
            verify(jedisPool).returnResource(jedis);
        }
    }

    @Test
    public void givenJedisConnectionProblemWhenLookingUpAnalysisResultThenThrowException() throws Exception {
        UUID analysisId = UUID.randomUUID();

        when(jedis.get(analysisId.toString()))
                .thenThrow(new JedisConnectionException("error"));

        try {
            analyses.lookup(analysisId);
            fail();
        } catch (AnalysesException expected) {
            verify(jedisPool).getResource();
            verify(jedisPool).returnBrokenResource(jedis);
        }
    }

    @Test
    public void givenSomeOtherProblemWhenLookingUpAnalysisResultThenThrowException() throws Exception {
        UUID analysisId = UUID.randomUUID();

        when(jedis.get(analysisId.toString())).thenThrow(new RuntimeException());

        try {
            analyses.lookup(analysisId);
            fail();
        } catch (AnalysesException expected) {
            verify(jedisPool).getResource();
            verify(jedisPool).returnResource(jedis);
        }
    }
}
