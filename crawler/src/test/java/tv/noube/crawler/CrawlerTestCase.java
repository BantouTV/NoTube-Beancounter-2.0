package tv.noube.crawler;

import org.testng.Assert;
import org.testng.annotations.Test;
import tv.notube.crawler.CrawlerException;
import tv.notube.crawler.DefaultCrawlerFactory;
import tv.notube.crawler.ParallelCrawlerImpl;
import tv.notube.crawler.Report;
import tv.notube.usermanager.UserManager;

import java.io.IOException;
import java.util.UUID;

/**
 * Reference test case for {@link tv.notube.crawler.Crawler}
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class CrawlerTestCase extends AbstractCrawlerTestCase {

    private UserManager userManager;

    private ParallelCrawlerImpl crawler;

    protected CrawlerTestCase() {
        super(9995);
        userManager = new MockUserManager();
        crawler = new ParallelCrawlerImpl(userManager);
    }

    @Test
     public void testCrawl() {
        Report report = null;
        try {
            report = crawler.crawl();
        } catch (CrawlerException e) {
            e.printStackTrace();
        }
        logger.info("report: " + report.toString());
        Assert.assertNotNull(report);
    }

    @Test
    public void testCrawlUUID() {
        Report report = null;
        try {
            report = crawler.crawl(UUID.randomUUID());
        } catch (CrawlerException e) {
            e.printStackTrace();
        }
        logger.info("report: " + report.toString());
        Assert.assertNotNull(report);
    }
}