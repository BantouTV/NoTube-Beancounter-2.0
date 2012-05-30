package tv.noube.crawler;

import com.google.inject.AbstractModule;

import tv.notube.crawler.Crawler;
import tv.notube.crawler.ParallelCrawlerImpl;
import tv.notube.crawler.requester.Requester;
import tv.notube.usermanager.UserManager;

/**
 * 
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class TestCrawlerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UserManager.class).to(MockUserManager.class);
        bind(Requester.class).to(MockRequester.class);
        bind(Crawler.class).to(ParallelCrawlerImpl.class);
    }
}