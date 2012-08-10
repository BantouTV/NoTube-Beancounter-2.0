package tv.notube.jmspublisher.process;

import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.naming.NamingException;

import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.guice.CamelModuleWithMatchingRoutes;
import org.guiceyfruit.jndi.JndiBind;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;

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
    JmsComponent jms(@Named("connectionfactory") String connectionfactory,
                     @Named("jms.naming.provider.url") String providerUrl,
                     @Named("jms.naming.factory.url.pkgs") String urlPkgs,
                     @Named("jms.naming.factory.initial") String factoryInitial) {
        Properties properties = new Properties();
//        properties.put("java.naming.provider.url", "jnp://localhost:1099");
        properties.put("java.naming.provider.url", providerUrl);
//        properties.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
        properties.put("java.naming.factory.initial", factoryInitial);
//        properties.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
        properties.put("java.naming.factory.url.pkgs", urlPkgs);
        JndiTemplate jndiTemplate = new JndiTemplate(properties);

        JndiObjectFactoryBean objectFactoryBean = new JndiObjectFactoryBean();
        objectFactoryBean.setJndiTemplate(jndiTemplate);
//        objectFactoryBean.setJndiName("/ConnectionFactory");
        objectFactoryBean.setJndiName(connectionfactory);
        objectFactoryBean.setResourceRef(true);
        try {
            objectFactoryBean.afterPropertiesSet();
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }

        ConnectionFactory connectionFactory = (ConnectionFactory)objectFactoryBean.getObject();
        return JmsComponent.jmsComponent(connectionFactory);
    }
}