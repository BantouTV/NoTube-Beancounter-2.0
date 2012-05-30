package tv.noube.crawler.runnable;

import com.google.inject.Guice;
import com.google.inject.Injector;
import junit.framework.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.tests.TestsBuilder;
import tv.notube.crawler.requester.Requester;
import tv.notube.crawler.runnable.Spider;
import tv.notube.crawler.runnable.SpiderException;
import tv.notube.usermanager.UserManager;
import tv.notube.usermanager.UserManagerException;
import tv.noube.crawler.TestCrawlerModule;
import tv.noube.crawler.VerbRandomiser;

import java.util.List;
import java.util.UUID;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class SpiderTestCase {

    private UserManager userManager;

    private Requester requester;

    private List<UUID> UUIDs;

    @BeforeTest
    public void setUp() throws UserManagerException {
        Injector injector = Guice.createInjector(new TestCrawlerModule());
        userManager = injector.getInstance(UserManager.class);
        requester = injector.getInstance(Requester.class);
        UUIDs = userManager.getUsersToCrawled();
        TestsBuilder.getInstance().build().register(new VerbRandomiser("verb-randomiser"));
    }

    @Test
    public void testRun() throws UserManagerException, SpiderException {
        UUID actualUUID = UUIDs.get(0);
        Assert.assertNull(userManager.getUserActivities(actualUUID));
        Spider spider = new Spider(
                "spider-name",
                userManager,
                UUIDs.get(0),
                requester
        );
        spider.run();
        List<Activity> expectedAs = userManager.getUserActivities(actualUUID);
        Assert.assertNotNull(userManager.getUserActivities(actualUUID));
        int maxActivities = 100 * userManager.getUser(actualUUID).getServices().size();
        org.testng.Assert.assertTrue(expectedAs.size() >= 0 && expectedAs.size() <= maxActivities);

    }

}