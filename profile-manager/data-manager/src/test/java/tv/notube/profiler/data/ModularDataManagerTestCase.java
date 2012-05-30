package tv.notube.profiler.data;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.configuration.profiler.DataManagerConfiguration;
import tv.notube.commons.configuration.profiler.DataManagerConfigurationException;
import tv.notube.commons.model.UserActivities;
import tv.notube.usermanager.DefaultUserManagerFactory;
import tv.notube.usermanager.UserManager;
import tv.notube.usermanager.UserManagerFactoryException;

import java.util.ArrayList;
import java.util.List;

// TODO (low) understand why this test keeps running also disabled
public class ModularDataManagerTestCase {

    private static Logger logger = Logger.getLogger(ModularDataManagerTestCase.class);

    private DataManager dataManager;

    //@BeforeTest(enabled = false)
    public void setUp() throws UserManagerFactoryException, DataManagerConfigurationException, DataManagerException {
        UserManager um = DefaultUserManagerFactory.getInstance().build();
        DataManagerConfiguration dataManagerConfiguration = new DataManagerConfiguration();
        dataManagerConfiguration.registerKey(
                "user",
                "test-profiling-line",
                "tv.notube.profiler.data.datasources.UserDataSource"
        );
        dataManager = new ModularDataManager(um, dataManagerConfiguration);
    }

    //@AfterTest(enabled = false)
    public void tearDown() {
        dataManager = null;
    }

    //@Test(enabled = false)
    public void testGetUserRawData() throws DataManagerException {
        RawDataSet<UserActivities> userRawDataSet =
                dataManager.getRawData("user");
        Assert.assertNotNull(userRawDataSet);
        Assert.assertTrue(userRawDataSet.size() >= 0);
        while (userRawDataSet.hasNext()) {
            UserActivities userActivities = userRawDataSet.getNext();
            Assert.assertNotNull(userActivities);
            logger.info("Retrieved activities: '" + userActivities + "'");
        }
    }

    //@Test(enabled = false)
    public void testGetProfilingLinesForAKey() throws DataManagerException {
        List<String> userProfilingLines = dataManager.getRegisteredKeys().get("user");
        List<String> expectedUserProfilingLines = new ArrayList<String>();
        expectedUserProfilingLines.add("test-profiling-line");
        Assert.assertTrue(expectedUserProfilingLines.equals(userProfilingLines));
    }
}
