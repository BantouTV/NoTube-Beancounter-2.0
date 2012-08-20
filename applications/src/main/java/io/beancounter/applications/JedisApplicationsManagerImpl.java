package io.beancounter.applications;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import io.beancounter.applications.model.Application;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class JedisApplicationsManagerImpl implements ApplicationsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(JedisApplicationsManagerImpl.class);

    @Inject
    @Named("redis.db.applications") int database;

    private JedisPool pool;

    private ObjectMapper mapper;

    @Inject
    public JedisApplicationsManagerImpl(JedisPoolFactory factory) {
        pool = factory.build();
        mapper = new ObjectMapper();
    }

    @Override
    public UUID registerApplication(
            String name,
            String description,
            String email,
            URL callback
    ) throws ApplicationsManagerException {
        Application application = Application.build(
                name,
                description,
                email,
                callback
        );
        String applicationJson;
        try {
            applicationJson = mapper.writeValueAsString(application);
        } catch (IOException e) {
            final String errMsg = "Error while getting json for app [" + name + "]";
            LOGGER.error(errMsg, e);
            throw new ApplicationsManagerException(
                    errMsg,
                    e
            );
        }
        Jedis jedis;
        try {
            jedis = pool.getResource();
        } catch (Exception e) {
            final String errMsg = "Error while getting a Jedis resource";
            LOGGER.error(errMsg, e);
            throw new ApplicationsManagerException(errMsg, e);
        }
        try {
            jedis.select(database);
        } catch (Exception e) {
            pool.returnResource(jedis);
            final String errMsg = "Error while selecting database [" + database + "] for app [" + name + "]";
            LOGGER.error(errMsg, e);
            throw new ApplicationsManagerException(errMsg, e);
        }
        try {
            jedis.set(application.getApiKey().toString(), applicationJson);
        } catch (Exception e) {
            final String errMsg = "Error while storing application [" + applicationJson + "] with id [" +
                    application.getApiKey().toString() + "]";
            LOGGER.error(errMsg, e);
            throw new ApplicationsManagerException(errMsg, e);
        } finally {
            pool.returnResource(jedis);
        }
        return application.getApiKey();
    }

    @Override
    public void deregisterApplication(UUID key) throws ApplicationsManagerException {
        Jedis jedis;
        try {
            jedis = pool.getResource();
        } catch (Exception e) {
            final String errMsg = "Error while getting a Jedis resource";
            LOGGER.error(errMsg, e);
            throw new ApplicationsManagerException(errMsg, e);
        }
        try {
            jedis.select(database);
        } catch (Exception e) {
            pool.returnResource(jedis);
            final String errMsg = "Error while selecting database [" + database + "] for app with id [" + key + "]";
            LOGGER.error(errMsg, e);
            throw new ApplicationsManagerException(errMsg, e);
        }
        try {
            jedis.del(key.toString());
        } catch (Exception e) {
            final String errMsg = "Error while deleting application with id [" + key.toString() + "]";
            LOGGER.error(errMsg, e);
            throw new ApplicationsManagerException(errMsg, e);
        } finally {
            pool.returnResource(jedis);
        }
    }

    @Override
    public synchronized Application getApplicationByApiKey(UUID key) throws ApplicationsManagerException {
        Jedis jedis;
        try {
            jedis = pool.getResource();
        } catch (Exception e) {
            final String errMsg = "Error while getting a Jedis resource";
            LOGGER.error(errMsg, e);
            throw new ApplicationsManagerException(errMsg, e);
        }
        try {
            jedis.select(database);
        } catch (Exception e) {
            pool.returnResource(jedis);
            final String errMsg = "Error while selecting database [" + database + "] for app with id [" + key + "]";
            LOGGER.error(errMsg, e);
            throw new ApplicationsManagerException(errMsg, e);
        }
        String applicationJson;
        try {
            applicationJson = jedis.get(key.toString());
        } catch (Exception e) {
            final String errMsg = "Error while retrieving application with id [" + key.toString() + "]";
            LOGGER.error(errMsg, e);
            throw new ApplicationsManagerException(errMsg, e);
        } finally {
            pool.returnResource(jedis);
        }
        if(applicationJson == null) {
            return null;
        }
        try {
            return mapper.readValue(applicationJson, Application.class);
        } catch (IOException e) {
            final String errMsg = "Error while getting json for app with key [" + key + "]";
            LOGGER.error(errMsg, e);
            throw new ApplicationsManagerException(
                    errMsg,
                    e
            );
        }
    }

    @Override
    public boolean isAuthorized(UUID key, Action action, Object object)
            throws ApplicationsManagerException {
        Application application = getApplicationByApiKey(key);
        if(application == null) {
            return false;
        }
        return application.hasPrivileges(action, object);
    }
}
