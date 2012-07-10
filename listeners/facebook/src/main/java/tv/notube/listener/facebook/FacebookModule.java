package tv.notube.listener.facebook;

import org.apache.camel.guice.CamelModuleWithMatchingRoutes;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class FacebookModule extends CamelModuleWithMatchingRoutes {

    public void configure() {
        super.configure();
        bind(FacebookListener.class);
    }

}