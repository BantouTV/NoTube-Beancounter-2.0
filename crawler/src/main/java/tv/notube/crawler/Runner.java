package tv.notube.crawler;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import tv.notube.synch.client.Helper;
import tv.notube.synch.client.SynchronizerClient;
import tv.notube.synch.client.SynchronizerClientException;
import tv.notube.synch.model.Released;

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
        }

        // ask token and wait
        // TODO (high) make it configurable
        Helper helper = Helper.getInstance("http://moth.notube.tv:9090/service-1.0-SNAPSHOT/rest/synch");
        UUID token;
        try {
            token = helper.access("crawler");
        } catch (SynchronizerClientException e) {
            logger.fatal("Something went wrong while synchronizing", e);
            System.exit(-1);
            throw new RuntimeException(e);
        }

        logger.info("Crawl started at: " + new DateTime(System.currentTimeMillis()));
        Report report;
        try {
            report = crawler.crawl();
        } catch (CrawlerException e) {
            try {
                helper.release("crawler", token);
            } catch (SynchronizerClientException e1) {
                logger.fatal("Something went wrong while synchronizing", e);
                System.exit(-1);
                throw new RuntimeException(e);
            }
            logger.fatal("Something went wrong while running the crawler", e);
            System.exit(-1);
            throw new RuntimeException();
        }

        try {
            helper.release("crawler", token);
        } catch (SynchronizerClientException e) {
            logger.fatal("Something went wrong while synchronizing", e);
            System.exit(-1);
            throw new RuntimeException(e);
        }
        logger.info("Crawl endend at: " + new DateTime(System.currentTimeMillis()));
        logger.info(report.getSubmittedProcesses()
                + " processes have been submitted in "
                + (report.getEndedAt() - report.getStartedAt()) / 1000
                + " seconds");
    }

}
