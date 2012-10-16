package io.beancounter.analyses;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import io.beancounter.commons.model.AnalysisResult;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class JedisAnalysesImpl implements Analyses {

    private static Logger LOGGER = LoggerFactory.getLogger(JedisAnalysesImpl.class);

    private JedisPool pool;

    private ObjectMapper mapper;

    @Inject
    @Named("redis.db.analyses") private int database;

    @Inject
    public JedisAnalysesImpl(JedisPoolFactory factory, ObjectMapper mapper) {
        pool = factory.build();
        this.mapper = mapper;
    }

    @Override
    public void store(AnalysisResult ar) throws AnalysesException {
        LOGGER.debug("storing result for analysis [" + ar.getAnalysis() + "]");

        String analysisJson;
        try {
            analysisJson = mapper.writeValueAsString(ar);
        } catch (Exception e) {
            final String errMsg = "Error while getting json for analysis result [" + ar.getAnalysis() + "]";
            LOGGER.error(errMsg, e);
            throw new AnalysesException(errMsg, e);
        }

        Jedis jedis = getJedisResource();
        boolean isConnectionIssue = false;
        try {
            jedis.set(ar.getAnalysis().toString(), analysisJson);
        } catch (JedisConnectionException jce) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while storing result for analysis [" + ar.getAnalysis() + "]";
            LOGGER.error(errMsg, jce);
            throw new AnalysesException(errMsg, jce);
        } catch (Exception ex) {
            final String errMsg = "Error while storing result for analysis [" + ar.getAnalysis() + "]";
            LOGGER.error(errMsg, ex);
            throw new AnalysesException(errMsg, ex);
        } finally {
            if (isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
            }
        }

        LOGGER.debug("result for analysis [" + ar.getAnalysis() + "] stored");
    }

    @Override
    public AnalysisResult lookup(UUID analysisId) throws AnalysesException {
        LOGGER.debug("looking up result for analysis [" + analysisId + "]");

        String resultJson;
        Jedis jedis = getJedisResource();
        boolean isConnectionIssue = false;

        try {
            resultJson = jedis.get(analysisId.toString());
        } catch (JedisConnectionException jce) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while retrieving result for analysis [" + analysisId + "]";
            LOGGER.error(errMsg, jce);
            throw new AnalysesException(errMsg, jce);
        } catch (Exception ex) {
            final String errMsg = "Error while retrieving result for analysis [" + analysisId + "]";
            LOGGER.error(errMsg, ex);
            throw new AnalysesException(errMsg, ex);
        } finally {
            if (isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
            }
        }

        if (resultJson == null) {
            return null;
        }

        try {
            LOGGER.debug("result for analysis [" + analysisId + "] looked up properly");
            return mapper.readValue(resultJson, AnalysisResult.class);
        } catch (Exception e) {
            final String errMsg = "Error while getting json for analysis [" + analysisId + "]";
            LOGGER.error(errMsg, e);
            throw new AnalysesException(errMsg, e);
        }
    }

    private Jedis getJedisResource() throws AnalysesException {
        Jedis jedis;
        try {
            jedis = pool.getResource();
        } catch (Exception e) {
            final String errMsg = "Error while getting a Jedis resource";
            LOGGER.error(errMsg, e);
            throw new AnalysesException(errMsg, e);
        }
        boolean isConnectionIssue = false;
        try {
            jedis.select(database);
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while selecting database [" + database + "]";
            LOGGER.error(errMsg, e);
            throw new AnalysesException(errMsg, e);
        } catch (Exception e) {
            pool.returnResource(jedis);
            final String errMsg = "Error while selecting database [" + database + "]";
            LOGGER.error(errMsg, e);
            throw new AnalysesException(errMsg, e);
        } finally {
            if(isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            }
        }
        return jedis;
    }

}
