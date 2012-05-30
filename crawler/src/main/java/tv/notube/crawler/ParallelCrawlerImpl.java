package tv.notube.crawler;

import com.google.inject.Inject;
import org.apache.log4j.Logger;
import tv.notube.crawler.requester.DefaultRequester;
import tv.notube.crawler.requester.Requester;
import tv.notube.crawler.runnable.Spider;
import tv.notube.crawler.runnable.SpiderException;
import tv.notube.usermanager.UserManager;
import tv.notube.usermanager.UserManagerException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ParallelCrawlerImpl extends AbstractCrawler {

    private static Logger logger = Logger.getLogger(ParallelCrawlerImpl.class);

    private ExecutorService executor = Executors.newCachedThreadPool();

    private Requester requester;

    @Inject
    public ParallelCrawlerImpl(
            UserManager userManager,
            Requester requester)
    {
        super(userManager);
        this.requester = requester;
    }

    public Report crawl() throws CrawlerException {
        long start = System.currentTimeMillis();
        List<UUID> ids;
        try {
            ids = getUserManager().getUsersToCrawled();
        } catch (UserManagerException e) {
            final String errMsg = "Error while retrieving users to be crawled";
            logger.error(errMsg, e);
            throw new CrawlerException(errMsg, e);
        }
        int count = 0;
        for(UUID id : ids) {
            if(!executor.isShutdown()) {
                count++;
                try {
                    executor.submit(new Spider(
                            "runnable-" + id.toString(),
                            getUserManager(),
                            id,
                            requester)
                    );
                } catch (SpiderException e) {
                    final String errMsg = "Error while instantiating Job '" + id.toString() + "'";
                    logger.error(errMsg, e);
                    throw new CrawlerException(errMsg ,e);
                }
            }
        }
        executor.shutdown();
        long end = System.currentTimeMillis();
        return new Report(count, start, end);
    }

    public Report crawl(UUID userId) throws CrawlerException {
        Spider spider;
        try {
            spider = new Spider(
                    "runnable-single-" + userId.toString(),
                    getUserManager(),
                    userId,
                    requester
            );
        } catch (SpiderException e) {
            throw new CrawlerException(
                    "Error while crawling data for [" + userId.toString() + "]",
                    e
            );
        }
        long start = System.currentTimeMillis();
        spider.run();
        long end = System.currentTimeMillis();
        return new Report(1, start, end);
    }

    public boolean isCompleted() {
        return executor.isTerminated();
    }

}
