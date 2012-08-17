package io.beancounter.commons.helper.resolver;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Services {

    private Map<String, Integer> services = new HashMap<String, Integer>();

    public void put(String service, int redisDB) {
        services.put(service, redisDB);
    }

    public int get(String service) {
        return services.get(service);
    }

    private static final String RESOLVER = "resolver";

    public static Services build(Properties properties) {
        String declaredServices = properties.getProperty(
                RESOLVER + "." + "services"
        );
        if(declaredServices == null) {
            throw new RuntimeException("It seems you have not declared any services");
        }
        Services servicesObj = new Services();
        String services[] = declaredServices.split(",");
        for(String service : services) {
            String db = property(properties, "services", service);
            servicesObj.put(service, Integer.parseInt(db, 10));
        }
        return servicesObj;
    }

    private static String property(
            Properties properties,
            boolean optional,
            String... names
    ) {
        String key = RESOLVER;
        for(String name : names) {
            key += "." + name;
        }
        String result = properties.getProperty(key);
        if(!optional && result == null) {
            throw new RuntimeException("[" + key + "] is null");
        }
        return result;
    }

    private static String property(Properties properties, String... names) {
        return property(properties, false, names);
    }

}
