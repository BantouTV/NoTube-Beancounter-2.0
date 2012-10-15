package io.beancounter.analyses;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import io.beancounter.commons.helper.PropertiesHelper;
import io.beancounter.commons.helper.jedis.DefaultJedisPoolFactory;
import io.beancounter.commons.helper.jedis.JedisPoolFactory;

import java.util.Properties;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class AnalysesModule extends AbstractModule {

    @Override
    protected void configure() {
        Properties properties = PropertiesHelper.readFromClasspath("/redis.properties");
        Names.bindProperties(binder(), properties);
        bind(JedisPoolFactory.class).to(DefaultJedisPoolFactory.class).asEagerSingleton();
        bind(Analyses.class).to(JedisAnalysesImpl.class);
    }
}
