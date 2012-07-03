package tv.notube.usermanager.services;

import org.testng.Assert;
import org.testng.annotations.Test;
import tv.notube.commons.helper.PropertiesHelper;
import tv.notube.usermanager.services.auth.DefaultServiceAuthorizationManager;
import tv.notube.usermanager.services.auth.ServiceAuthorizationManager;

import java.util.Properties;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class DefaultServiceAuthorizationManagerTestCase {

    @Test
    public void test() {
        Properties properties = PropertiesHelper.readFromClasspath("/sam.properties");
        ServiceAuthorizationManager sam = DefaultServiceAuthorizationManager.build(properties);
        Assert.assertNotNull(sam);
    }

}
