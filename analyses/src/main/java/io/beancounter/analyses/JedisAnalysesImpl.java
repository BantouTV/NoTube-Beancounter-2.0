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

import java.io.IOException;
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
    public JedisAnalysesImpl(JedisPoolFactory factory) {
        pool = factory.build();
        mapper = new ObjectMapper();
    }

    @Override
    public void store(AnalysisResult ar) throws AnalysesException {
        LOGGER.debug("storing result for analysis [" + ar.getAnalysis() + "]");
        String analysisJson;
        try {
            analysisJson = mapper.writeValueAsString(ar);
        } catch (IOException e) {
            final String errMsg = "Error while getting json for analysis result [" + ar.getAnalysis() + "]";
            LOGGER.error(errMsg, e);
            throw new AnalysesException(
                    errMsg,
                    e
            );
        } catch (Exception e) {
            final String errMsg = "Error while getting json for analysis result [" +  ar.getAnalysis() + "]";
            LOGGER.error(errMsg, e);
            throw new AnalysesException(
                    errMsg,
                    e
            );
        }
        Jedis jedis = getJedisResource();
        LOGGER.debug("storing result for analysis [" + ar.getAnalysis() + "] on database [" + database + "]");
        boolean isConnectionIssue = false;
        try {
            jedis.set(ar.getAnalysis().toString(), analysisJson);
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while storing result for analysis [" + ar.getAnalysis() + "]";
            LOGGER.error(errMsg, e);
            throw new AnalysesException(errMsg, e);
        } catch (Exception e) {
            final String errMsg = "Error while storing result for analysis [" + ar.getAnalysis() + "]";
            LOGGER.error(errMsg, e);
            throw new AnalysesException(errMsg, e);
        } finally {
            if(isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
            }
        }
        LOGGER.debug("result for analysis [" + ar.getAnalysis() + "] stored");
    }

    @Override
    public AnalysisResult lookup(UUID analysesId) throws AnalysesException {
        LOGGER.debug("looking up result for analysis [" + analysesId + "]");
        AnalysisResult analysisResult;
        Jedis jedis = getJedisResource();
        String resultJson;
        boolean isConnectionIssue = false;
        try {
            resultJson = jedis.get(analysesId.toString());
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Error while retrieving result for analysis [" + analysesId + "]";
            LOGGER.error(errMsg, e);
            throw new AnalysesException(errMsg, e);
        } catch (Exception e) {
            final String errMsg = "Error while retrieving result for analysis [" + analysesId + "]";
            LOGGER.error(errMsg, e);
            throw new AnalysesException(errMsg, e);
        } finally {
            if(isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
            }
        }
        if(resultJson == null) {
            return null;
        }
        try {
            analysisResult = mapper.readValue(resultJson, AnalysisResult.class);
        } catch (IOException e) {
            final String errMsg = "Error while getting json for analysis [" + analysesId + "]";
            LOGGER.error(errMsg, e);
            throw new AnalysesException(
                    errMsg,
                    e
            );
        }
        LOGGER.debug("result for analysis [" + analysesId + "] looked up properly");
        return analysisResult;
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
