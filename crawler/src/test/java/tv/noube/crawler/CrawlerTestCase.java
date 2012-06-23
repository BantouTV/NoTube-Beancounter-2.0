package tv.noube.crawler;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.tests.TestsBuilder;
import tv.notube.crawler.Crawler;
import tv.notube.crawler.CrawlerException;
import tv.notube.crawler.Report;

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
    public void testCrawlUUID() throws CrawlerException {
        Report report;
        report = crawler.crawl("test-username");
        Assert.assertNotNull(report);
    }
}