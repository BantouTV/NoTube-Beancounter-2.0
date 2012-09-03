package io.beancounter.filter.manager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import io.beancounter.filter.model.Filter;
import io.beancounter.filter.model.pattern.ActivityPattern;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Redis-based implementation of {@link FilterManager}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Singleton
public class JedisFilterManager implements FilterManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(JedisFilterManager.class);

    private static final String CHANNEL = "filters";

    private JedisPool pool;

    @Inject
    @Named("redis.db.filters")
    private int database;

    private ObjectMapper objectMapper;

    @Inject
    public JedisFilterManager(
            JedisPoolFactory factory
    ) {
        pool = factory.build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String register(
            String name,
            String description,
            Set<String> queues,
            ActivityPattern activityPattern
    ) throws FilterManagerException {
        if (get(name) != null) {
            final String errMsg = "Filter [" + name + "] already exists";
            LOGGER.error(errMsg);
            throw new FilterManagerException(errMsg);
        }
        Filter filter = new Filter(
                name,
                description,
                activityPattern,
                queues
        );
        String filterJson;
        try {
            filterJson = objectMapper.writeValueAsString(filter);
        } catch (IOException e) {
            final String errMsg = "Error while converting filter [" + name + "] to json";
            LOGGER.error(errMsg, e);
            throw new FilterManagerException(errMsg, e);
        }
        Jedis jedis = getJedisResource();
        boolean isConnectionIssue = false;
        try {
            jedis.set(name, filterJson);
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while registering filter [" + name + "]";
            LOGGER.error(errMsg, e);
            throw new FilterManagerException(errMsg, e);
        } catch (Exception e) {
            final String errMsg = "Error while registering filter [" + name + "]";
            LOGGER.error(errMsg, e);
            throw new FilterManagerException(errMsg, e);
        } finally {
            if(isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
            }
        }
        return name;
    }

    @Override
    public Filter get(String name) throws FilterManagerException {
        Jedis jedis = getJedisResource();
        String filterJson;
        boolean isConnectionIssue = false;
        try {
            filterJson = jedis.get(name);
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while retrieving filter [" + name + "]";
            LOGGER.error(errMsg, e);
            throw new FilterManagerException(errMsg, e);
        } catch (Exception e) {
            final String errMsg = "Error while retrieving filter [" + name + "]";
            LOGGER.error(errMsg, e);
            throw new FilterManagerException(errMsg, e);
        } finally {
            if(isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
            }
        }
        if (filterJson == null) {
            return null;
        }
        Filter filter;
        try {
            filter = objectMapper.readValue(filterJson, Filter.class);
        } catch (IOException e) {
            final String errMsg = "Error while converting filter [" + name + "] from json";
            LOGGER.error(errMsg, e);
            throw new FilterManagerException(errMsg, e);
        }
        return filter;
    }

    @Override
    public void delete(String name) throws FilterManagerException {
        if (get(name) == null) {
            final String errMsg = "Filter [" + name + "] does not exist";
            LOGGER.error(errMsg);
            throw new FilterManagerException(errMsg);
        }

        // then delete it
        Jedis jedis = getJedisResource();
        boolean isConnectionIssue = false;
        try {
            jedis.del(name);
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while deleting filter [" + name + "]";
            LOGGER.error(errMsg, e);
            throw new FilterManagerException(errMsg, e);
        } catch (Exception e) {
            final String errMsg = "Error while deleting filter [" + name + "]";
            LOGGER.error(errMsg, e);
            throw new FilterManagerException(errMsg, e);
        } finally {
            if(isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
            }
        }
        // notify the filter has been deleted
        notify(name);
    }

    @Override
    public void start(String name) throws FilterManagerException {
        if (get(name) == null) {
            final String errMsg = "Filter [" + name + "] does not exist";
            LOGGER.error(errMsg);
            throw new FilterManagerException(errMsg);
        }
        Filter filter = get(name);
        filter.setActive(true);
        update(filter);
        // notify the filter has been started
        notify(filter.getName());
    }

    private void notify(String name) throws FilterManagerException {
        Jedis jedis = getJedisResource();
        boolean isConnectionIssue = false;
        try {
            jedis.publish(CHANNEL, name);
        } catch (JedisConnectionException e) {
            final String errMsg = "Jedis Connection error while publishing filter [" + name + "]";
            LOGGER.error(errMsg, e);
            throw new FilterManagerException(errMsg, e);
        } catch (Exception e) {
            final String errMsg = "Error while publishing filter [" + name + "]";
            LOGGER.error(errMsg, e);
            throw new FilterManagerException(errMsg, e);
        } finally {
            if(isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
            }
        }
    }

    @Override
    public void stop(String name) throws FilterManagerException {
        if (get(name) == null) {
            final String errMsg = "Filter [" + name + "] does not exist";
            LOGGER.error(errMsg);
            throw new FilterManagerException(errMsg);
        }
        Filter filter = get(name);
        filter.setActive(false);
        update(filter);
        // notify the filter has been stopped
        notify(filter.getName());
    }

    @Override
    public void update(Filter filter) throws FilterManagerException {
        String name = filter.getName();
        String filterJson;
        try {
            filterJson = objectMapper.writeValueAsString(filter);
        } catch (IOException e) {
            final String errMsg = "Error while converting filter [" + name + "] to json";
            LOGGER.error(errMsg, e);
            throw new FilterManagerException(errMsg, e);
        }
        Jedis jedis = getJedisResource();
        boolean isConnectionIssue = false;
        try {
            jedis.set(name, filterJson);
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while updating filter [" + name + "]";
            LOGGER.error(errMsg, e);
            throw new FilterManagerException(errMsg, e);
        } catch (Exception e) {
            final String errMsg = "Error while updating filter [" + name + "]";
            LOGGER.error(errMsg, e);
            throw new FilterManagerException(errMsg, e);
        } finally {
            if(isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
            }
        }
    }

    @Override
    public Collection<String> getRegisteredFilters() throws FilterManagerException {
        Jedis jedis = getJedisResource();
        Set<String> keys;
        boolean isConnectionIssue = false;
        try {
            keys = jedis.keys("*");
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while retrieving filters";
            LOGGER.error(errMsg, e);
            throw new FilterManagerException(errMsg, e);
        } catch (Exception e) {
            final String errMsg = "Error while retrieving filters";
            LOGGER.error(errMsg, e);
            throw new FilterManagerException(errMsg, e);
        }  finally {
            if(isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
            }
        }
        if (keys == null) {
            return new ArrayList<String>();
        }
        return new ArrayList<String>(keys);
    }

    private Jedis getJedisResource() throws FilterManagerException {
        Jedis jedis;
        try {
            jedis = pool.getResource();
        } catch (Exception e) {
            final String errMsg = "Error while getting a Jedis resource";
            LOGGER.error(errMsg, e);
            throw new FilterManagerException(errMsg, e);
        }
        boolean isConnectionIssue = false;
        try {
            jedis.select(database);
        } catch (JedisConnectionException e) {
            isConnectionIssue = true;
            final String errMsg = "Jedis Connection error while selecting database [" + database + "]";
            LOGGER.error(errMsg, e);
            throw new FilterManagerException(errMsg, e);
        } catch (Exception e) {
            pool.returnResource(jedis);
            final String errMsg = "Error while selecting database [" + database + "]";
            LOGGER.error(errMsg, e);
            throw new FilterManagerException(errMsg, e);
        } finally {
            if(isConnectionIssue) {
                pool.returnBrokenResource(jedis);
            }
        }
        return jedis;
    }
}
