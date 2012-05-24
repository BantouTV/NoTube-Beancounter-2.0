package tv.notube.crawler;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.UUID;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Runner {

    private static Logger logger = Logger.getLogger(Runner.class);

    private static Crawler crawler;

    public static void main(String[] args) {
        try {
            crawler = DefaultCrawlerFactory.getInstance().build();
        } catch (CrawlerFactoryException e) {
            logger.fatal("Something went wrong while instantiating the crawler", e);
            System.exit(-1);
            return;
        }
        logger.info("Crawl started at: " + new DateTime(System.currentTimeMillis()));
        Report report;
        try {
            report = crawler.crawl();
        } catch (CrawlerException e) {
            logger.fatal("Something went wrong while instantiating the crawler", e);
            System.exit(-1);
            return;
        }
        logger.info("Crawl endend at: " + new DateTime(System.currentTimeMillis()));
        logger.info(report.getSubmittedProcesses()
                + " processes have been submitted in "
                + (report.getEndedAt() - report.getStartedAt()) / 1000
                + " seconds");
    }

}
