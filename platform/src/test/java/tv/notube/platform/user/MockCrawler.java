package tv.notube.platform.user;

import org.joda.time.DateTime;
import tv.notube.crawler.Crawler;
import tv.notube.crawler.CrawlerException;
import tv.notube.crawler.Report;
import tv.notube.usermanager.UserManager;

import java.util.UUID;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */

public class MockCrawler implements Crawler {

    @Override
    public Report crawl() throws CrawlerException {
        return new Report(1, DateTime.now().getMillis(), DateTime.now().getMillis());
    }

    @Override
    public Report crawl(UUID userId) throws CrawlerException {
        return new Report(1, DateTime.now().getMillis(), DateTime.now().getMillis());
    }

    @Override
    public UserManager getUserManager() {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public boolean isCompleted() {
        throw new UnsupportedOperationException("NIY");
    }
}