package io.beancounter.commons.helper.resolver;

import org.testng.Assert;
import org.testng.annotations.Test;
import io.beancounter.commons.helper.PropertiesHelper;

import java.util.Properties;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ServicesTestCase {

    @Test
    public void test() {
        Properties properties = PropertiesHelper.readFromClasspath("/beancounter.properties");
        Services services = Services.build(properties);
        Assert.assertNotNull(services);
    }

}
