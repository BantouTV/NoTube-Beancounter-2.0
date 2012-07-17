package tv.notube.filter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import tv.notube.commons.helper.jedis.JedisPoolFactory;
import tv.notube.filter.model.Filter;
import tv.notube.filter.model.pattern.ActivityPattern;

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

    private JedisPool pool;

    @Inject
    @Named("redis.db.filters") private int db;

    private ObjectMapper objectMapper;

    @Inject
    public JedisFilterManager(
            JedisPoolFactory factory,
            int db
    ) {
        pool = factory.build();
        this.db = db;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String register(
            String name,
            String description,
            ActivityPattern activityPattern
    ) throws FilterManagerException {
        if(get(name) != null) {
            final String errMsg = "Filter [" + name + "] already exists";
            LOGGER.error(errMsg);
            throw new FilterManagerException(errMsg);
        }
        Filter filter = new Filter(
                name,
                description,
                activityPattern
        );
        String filterJson;
        try {
            filterJson = objectMapper.writeValueAsString(filter);
        } catch (IOException e) {
            final String errMsg = "Error while converting filter [" + name + "] to json";
            LOGGER.error(errMsg, e);
            throw new FilterManagerException(errMsg, e);
        }
        Jedis jedis = pool.getResource();
        jedis.select(db);
        try {
        jedis.set(name, filterJson);
        } finally {
            pool.returnResource(jedis);
        }
        return name;
    }

    @Override
    public Filter get(String name) throws FilterManagerException {
        Jedis jedis = pool.getResource();
        jedis.select(db);
        String filterJson;
        try {
            filterJson = jedis.get(name);
        } finally {
            pool.returnResource(jedis);
        }
        if(filterJson == null) {
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
        if(get(name) == null) {
            final String errMsg = "Filter [" + name + "] does not exist";
            LOGGER.error(errMsg);
            throw new FilterManagerException(errMsg);
        }
        Jedis jedis = pool.getResource();
        jedis.select(db);
        try {
        jedis.del(name);
        } finally {
            pool.returnResource(jedis);
        }
    }

    @Override
    public void start(String name) throws FilterManagerException {
        if(get(name) == null) {
            final String errMsg = "Filter [" + name + "] does not exist";
            LOGGER.error(errMsg);
            throw new FilterManagerException(errMsg);
        }
        Filter filter = get(name);
        filter.setActive(true);
        update(filter);
    }

    @Override
    public void stop(String name) throws FilterManagerException {
        if(get(name) == null) {
            final String errMsg = "Filter [" + name + "] does not exist";
            LOGGER.error(errMsg);
            throw new FilterManagerException(errMsg);
        }
        Filter filter = get(name);
        filter.setActive(false);
        update(filter);
    }

    @Override
    public void update(Filter filter) throws FilterManagerException {
        String name = filter.getName();
        if(get(name) != null) {
            final String errMsg = "Filter [" + name + "] already exists";
            LOGGER.error(errMsg);
            throw new FilterManagerException(errMsg);
        }
        String filterJson;
        try {
            filterJson = objectMapper.writeValueAsString(filter);
        } catch (IOException e) {
            final String errMsg = "Error while converting filter [" + name + "] to json";
            LOGGER.error(errMsg, e);
            throw new FilterManagerException(errMsg, e);
        }
        Jedis jedis = pool.getResource();
        jedis.select(db);
        try {
        jedis.set(name, filterJson);
        } finally {
            pool.returnResource(jedis);
        }
    }

    @Override
    public Collection<String> getRegisteredFilters() throws FilterManagerException {
        Jedis jedis = pool.getResource();
        jedis.select(db);
        Set<String> keys;
        try {
            keys = jedis.keys("*");
        } finally {
            pool.returnResource(jedis);
        }
        if(keys == null) {
            return new ArrayList<String>();
        }
        return new ArrayList<String>(keys);
    }
}
