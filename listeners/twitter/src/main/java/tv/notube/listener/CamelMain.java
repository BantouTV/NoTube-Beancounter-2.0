package tv.notube.listener;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.main.Main;

public class CamelMain {

    public static void main(String[] args) throws Exception {

        final PropertiesComponent pc = new PropertiesComponent();
        pc.setLocation("classpath:twitter.properties");


        // create a new Camel Main so we can easily start Camel
        Main main = new Main() {
            protected Map<String, CamelContext> getCamelContextMap() {
                Map<String, CamelContext> answer = new HashMap<String, CamelContext>();
                DefaultCamelContext defaultCamelContext = new DefaultCamelContext();
                defaultCamelContext.addComponent("properties", pc);
                answer.put("camel-1", defaultCamelContext);
                return answer;
            }
        };

        // enable hangup support which mean we detect when the JVM terminates, and stop Camel graceful
        main.enableHangupSupport();

        TwitterRoute route = new TwitterRoute();

        // add our routes to Camel
        main.addRouteBuilder(route);

        // and run, which keeps blocking until we terminate the JVM (or stop CamelContext)
        main.run();
    }

}

