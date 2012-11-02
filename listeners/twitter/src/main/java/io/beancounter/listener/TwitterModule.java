package io.beancounter.listener;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.inject.Provides;
import com.google.inject.name.Names;

import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.apache.camel.spi.LifecycleStrategy;
import org.guiceyfruit.jndi.JndiBind;

import io.beancounter.commons.helper.PropertiesHelper;
import io.beancounter.commons.helper.jedis.DefaultJedisPoolFactory;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;
import io.beancounter.commons.helper.resolver.Services;
import io.beancounter.resolver.JedisResolver;
import io.beancounter.resolver.Resolver;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class TwitterModule extends CamelModuleWithMatchingRoutes {

    @Override
    protected void configure() {
        super.configure();

        Properties redisProperties = PropertiesHelper.readFromClasspath("/redis.properties");
        Names.bindProperties(binder(), redisProperties);
        bindInstance("redisProperties", redisProperties);
        bind(JedisPoolFactory.class).to(DefaultJedisPoolFactory.class).asEagerSingleton();

        Properties properties = PropertiesHelper.readFromClasspath("/beancounter.properties");
        Services services = Services.build(properties);
        bind(Services.class).toInstance(services);
        bind(Resolver.class).to(JedisResolver.class);

        bind(TwitterRoute.class);
    }

    @Provides
    @JndiBind("properties")
    PropertiesComponent propertiesComponent() {
        PropertiesComponent pc = new PropertiesComponent();
        pc.setLocation("classpath:beancounter.properties");
        return pc;
    }


    @Provides
    List<LifecycleStrategy> lifecycleStrategy(JedisPoolFactory factory) {
        ArrayList<LifecycleStrategy> list = new ArrayList<LifecycleStrategy>();
        list.add(new ResourceCleanupStrategy(factory));
        return list;
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

