package io.beancounter.listener;

import org.apache.camel.CamelContext;
import org.apache.camel.VetoCamelContextStartException;
import org.apache.camel.support.LifecycleStrategySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.beancounter.commons.helper.jedis.JedisPoolFactory;

public class ResourceCleanupStrategy extends LifecycleStrategySupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceCleanupStrategy.class);

    JedisPoolFactory factory;

    public ResourceCleanupStrategy(JedisPoolFactory factory) {
        this.factory = factory;
    }

    @Override
    public void onContextStart(CamelContext context) throws VetoCamelContextStartException {
        LOGGER.info("Starting Camel handler");
    }

    @Override
    public void onContextStop(CamelContext context) {
        LOGGER.info("Stopping Camel handler");
        if (factory != null) {
            LOGGER.info("Closing Redis pool");
            factory.build().destroy();
        }
    }
}
