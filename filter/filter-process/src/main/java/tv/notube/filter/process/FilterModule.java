package tv.notube.filter.process;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import com.google.inject.Provides;
import com.google.inject.name.Names;

import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.apache.camel.spi.Registry;
import org.guiceyfruit.jndi.JndiBind;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import tv.notube.commons.helper.PropertiesHelper;
import tv.notube.filter.FilterService;
import tv.notube.filter.InMemoryFilterServiceImpl;
import tv.notube.filter.manager.FilterManager;
import tv.notube.filter.manager.InMemoryFilterManager;

public class FilterModule extends CamelModuleWithMatchingRoutes {

    @Override
    protected void configure() {
        super.configure();
        Properties redisProperties = PropertiesHelper.readFromClasspath("/redis.properties");
        Names.bindProperties(binder(), redisProperties);
        bindInstance("redisProperties", redisProperties);
        bind(FilterService.class).to(InMemoryFilterServiceImpl.class);
        bind(FilterManager.class).to(InMemoryFilterManager.class);
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
