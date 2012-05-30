package tv.notube.commons.storage.kvs.configuration;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ConfigurationManagerTestCase {

    private final static String CONFIG_FILE =
            "tv/notube/commons/storage/kvs/configuration/kvs-configuration.xml";

    private ConfigurationManager configurationManager;

    @BeforeTest
    public void setUp() {
        configurationManager = ConfigurationManager.getInstance(CONFIG_FILE);
    }

    @AfterTest
    public void tearDown() {
        configurationManager = null;
    }

    @Test(enabled = false)
    public void testGetConfigurations() {
        KVStoreConfiguration KVStoreConfiguration =
                configurationManager.getKVStoreConfiguration();
        Assert.assertNotNull(KVStoreConfiguration);
    }

}
