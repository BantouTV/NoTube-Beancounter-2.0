package tv.notube.crawler;

import tv.notube.usermanager.UserManager;

import java.util.UUID;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface Crawler {

    public Report crawl() throws CrawlerException;

    public Report crawl(UUID userId) throws CrawlerException;

    public UserManager getUserManager();

    public boolean isCompleted();

}
