package tv.noube.crawler;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.tests.TestsBuilder;
import tv.notube.crawler.Crawler;
import tv.notube.crawler.CrawlerException;
import tv.notube.crawler.Report;
import tv.notube.usermanager.UserManager;
import tv.notube.usermanager.UserManagerException;

import java.util.List;
import java.util.UUID;

/**
 * Reference test case for {@link tv.notube.crawler.Crawler}
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class CrawlerTestCase {

    private Crawler crawler;

    @BeforeTest
    public void setUp() {
        Injector injector = Guice.createInjector(new TestCrawlerModule());
        crawler = injector.getInstance(Crawler.class);
        TestsBuilder.getInstance().build().register(new VerbRandomiser("verb-randomiser"));
    }

    @Test
    public void testCrawl() throws CrawlerException, UserManagerException {
        UserManager userManager = crawler.getUserManager();
        List<UUID> expectedUUIDs = userManager.getUsersToCrawled();

        for (UUID expectedUUID : expectedUUIDs) {
            List<Activity> expectedAs = userManager.getUserActivities(expectedUUID);
            Assert.assertNull(expectedAs);
        }

        Report report = crawler.crawl();
        Assert.assertNotNull(report);

        Assert.assertEquals(report.getSubmittedProcesses(), expectedUUIDs.size());

        while(!crawler.isCompleted()) {}

        for (UUID expectedUUID : expectedUUIDs) {
            List<Activity> expectedAs = userManager.getUserActivities(expectedUUID);
            Assert.assertNotNull(expectedAs);
            int maxActivities = 100 * userManager.getUser(expectedUUID).getServices().size();
            Assert.assertTrue(expectedAs.size() >= 0 && expectedAs.size() <= maxActivities);
        }
    }

    @Test
    public void testCrawlUUID() {
        Report report = null;
        try {
            report = crawler.crawl(UUID.randomUUID());
        } catch (CrawlerException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(report);
    }
}