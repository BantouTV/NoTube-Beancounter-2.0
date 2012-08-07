package tv.notube.jmspublisher.process;

import java.util.Properties;

import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.guiceyfruit.jndi.JndiBind;

import tv.notube.commons.helper.PropertiesHelper;

public class JmsPublisherModule extends CamelModuleWithMatchingRoutes {

    @Override
    protected void configure() {
        super.configure();
        Properties properties = PropertiesHelper.readFromClasspath("/beancounter.properties");
        Names.bindProperties(binder(), properties);
        bind(JmsPublisherRoute.class);
        bind(ActivityToJmsConverter.class);
    }

    @Provides
    @JndiBind("properties")
    PropertiesComponent propertiesComponent() {
        PropertiesComponent pc = new PropertiesComponent();
        pc.setLocation("classpath:beancounter.properties");
        return pc;
    }


    @Provides
    @JndiBind("jms")
    JmsComponent jms(@Named("jms.broker.url") String brokerUrl) {
        return JmsComponent.jmsComponent(new ActiveMQConnectionFactory(brokerUrl));
    }


}