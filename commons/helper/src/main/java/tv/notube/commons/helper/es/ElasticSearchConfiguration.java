package tv.notube.commons.helper.es;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This models an Elastic Search configuration.
 *
 * @author Alex Cowell ( alxcwll@gmail.com )
 */
public class ElasticSearchConfiguration {

    private static final String ES = "es";

    public static ElasticSearchConfiguration build(Properties properties) {
        ElasticSearchConfiguration esc = new ElasticSearchConfiguration();
        String declaredNodes = properties.getProperty(
                ES + "." + "nodes"
        );
        if(declaredNodes == null) {
            throw new RuntimeException("It seems you have not declared any Elastic Search node");
        }
        String nodes[] = declaredNodes.split(",");
        for(String node : nodes) {
            String host = property(properties, "node", node, "host");
            String port = property(properties, "node", node, "port");
            NodeInfo nodeInfo = new NodeInfo(
                    host,
                    Integer.parseInt(port)
            );
            esc.addNode(nodeInfo);
        }
        return esc;
    }

    private List<NodeInfo> nodes = new ArrayList<NodeInfo>();

    public List<NodeInfo> getNodes() {
        return nodes;
    }

    public void addNode(NodeInfo node) {
        nodes.add(node);
    }

    private static String property(
            Properties properties,
            boolean optional,
            String... names
    ) {
        String key = ES;
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
