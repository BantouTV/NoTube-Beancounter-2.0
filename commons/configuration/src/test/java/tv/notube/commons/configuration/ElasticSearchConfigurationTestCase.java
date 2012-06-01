package tv.notube.commons.configuration;

import org.testng.annotations.Test;
import tv.notube.commons.configuration.activities.ElasticSearchConfiguration;
import tv.notube.commons.configuration.activities.NodeInfo;

import java.util.List;

import static org.testng.Assert.*;

/**
 * @author Alex Cowell ( alxcwll@gmail.com )
 */
public class ElasticSearchConfigurationTestCase {

    @Test
    public void loadValidElasticSearchConfigWithThreeNodes() throws Exception {
        ElasticSearchConfiguration configuration = Configurations.getConfiguration(
                "elasticsearch-configuration.xml",
                ElasticSearchConfiguration.class
        );

        assertNotNull(configuration);
        assertEquals(configuration.getNodes().size(), 3);

        List<NodeInfo> nodes = configuration.getNodes();

        assertEquals(nodes.get(0).getHost(), "localhost");
        assertEquals(nodes.get(0).getPort(), 9200);

        assertEquals(nodes.get(1).getHost(), "127.0.0.1");
        assertEquals(nodes.get(1).getPort(), 9201);

        assertEquals(nodes.get(2).getHost(), "123.123.123.123");
        assertEquals(nodes.get(2).getPort(), 9300);
    }

    @Test
    public void emptyElasticSearchConfigShouldHaveNoNodes() throws Exception {
        ElasticSearchConfiguration configuration = Configurations.getConfiguration(
                "empty-elasticsearch-configuration.xml",
                ElasticSearchConfiguration.class
        );

        assertNotNull(configuration);
        assertEquals(configuration.getNodes().size(), 0);
    }

    @Test
    public void invalidElasticSearchConfigShouldHaveNoNodes() throws Exception {
        // TODO: How should invalid properties be handled? Fail fast or quietly?
        ElasticSearchConfiguration configuration = Configurations.getConfiguration(
                "invalid-elasticsearch-configuration.xml",
                ElasticSearchConfiguration.class
        );

        assertNotNull(configuration);
        assertEquals(configuration.getNodes().size(), 0);
    }
}
