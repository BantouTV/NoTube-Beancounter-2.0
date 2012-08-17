package io.beancounter.usermanager.services;

import org.testng.Assert;
import org.testng.annotations.Test;
import io.beancounter.commons.helper.PropertiesHelper;
import io.beancounter.usermanager.services.auth.DefaultServiceAuthorizationManager;
import io.beancounter.usermanager.services.auth.ServiceAuthorizationManager;

import java.util.Properties;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class DefaultServiceAuthorizationManagerTestCase {

    @Test
    public void test() {
        Properties properties = PropertiesHelper.readFromClasspath("/beancounter.properties");
        ServiceAuthorizationManager sam = DefaultServiceAuthorizationManager.build(properties);
        Assert.assertNotNull(sam);
    }

}
