package tv.notube.resolver;

import java.util.HashMap;
import java.util.Map;

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
}
