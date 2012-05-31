package tv.notube.commons.configuration.activities;

import tv.notube.commons.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex Cowell ( alxcwll@gmail.com )
 */
public class ElasticSearchConfiguration extends Configuration {

    private List<NodeInfo> nodes = new ArrayList<NodeInfo>();

    public List<NodeInfo> getNodes() {
        return nodes;
    }

    public void addNode(NodeInfo node) {
        nodes.add(node);
    }
}
