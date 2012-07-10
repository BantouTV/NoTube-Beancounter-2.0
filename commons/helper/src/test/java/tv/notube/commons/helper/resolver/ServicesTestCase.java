package tv.notube.commons.helper.resolver;

import org.testng.Assert;
import org.testng.annotations.Test;
import tv.notube.commons.helper.PropertiesHelper;
import tv.notube.commons.helper.es.ElasticSearchConfiguration;

import java.util.Properties;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ServicesTestCase {

    @Test
    public void test() {
        Properties properties = PropertiesHelper.readFromClasspath("/resolver.properties");
        Services services = Services.build(properties);
        Assert.assertNotNull(services);
    }

}
