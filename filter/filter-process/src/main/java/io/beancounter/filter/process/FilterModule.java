package io.beancounter.filter.process;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import com.google.inject.Provides;
import com.google.inject.name.Names;

import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.guiceyfruit.jndi.JndiBind;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import io.beancounter.commons.helper.PropertiesHelper;
import io.beancounter.commons.helper.jedis.DefaultJedisPoolFactory;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import io.beancounter.filter.FilterService;
import io.beancounter.filter.InMemoryFilterServiceImpl;
import io.beancounter.filter.manager.FilterManager;
import io.beancounter.filter.manager.JedisFilterManager;

public class FilterModule extends CamelModuleWithMatchingRoutes {

    @Override
    protected void configure() {
        super.configure();
        Properties redisProperties = PropertiesHelper.readFromClasspath("/redis.properties");
        Names.bindProperties(binder(), redisProperties);
        bindInstance("redisProperties", redisProperties);
        bind(JedisPoolFactory.class).to(DefaultJedisPoolFactory.class).asEagerSingleton();
        bind(FilterManager.class).to(JedisFilterManager.class);
        bind(FilterService.class).to(InMemoryFilterServiceImpl.class);
        bind(FilterRoute.class);
    }

    @Provides
    @JndiBind("properties")
    PropertiesComponent propertiesComponent() {
        PropertiesComponent pc = new PropertiesComponent();
        pc.setLocation("classpath:beancounter.properties");
        return pc;
    }

    @Provides
    @JndiBind("serializer")
    RedisSerializer redisSerializer() {
        return new RedisSerializer<String>() {
            private static final String CHARSET = "UTF-8";

            @Override
            public byte[] serialize(String s) throws SerializationException {
                try {
                    return s.getBytes(CHARSET);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String deserialize(byte[] bytes) throws SerializationException {
                try {
                    return new String(bytes, CHARSET);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
