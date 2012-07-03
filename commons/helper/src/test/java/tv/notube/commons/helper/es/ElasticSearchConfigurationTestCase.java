package tv.notube.commons.helper.es;

import org.testng.Assert;
import org.testng.annotations.Test;
import tv.notube.commons.helper.PropertiesHelper;

import java.util.Properties;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ElasticSearchConfigurationTestCase {

    @Test
    public void test() {
        Properties properties = PropertiesHelper.readFromClasspath("/es.properties");
        ElasticSearchConfiguration esc = ElasticSearchConfiguration.build(properties);
        Assert.assertNotNull(esc);
    }

}
