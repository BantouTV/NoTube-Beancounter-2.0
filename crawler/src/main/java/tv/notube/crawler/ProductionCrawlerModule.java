package tv.notube.crawler;

import com.google.inject.AbstractModule;
import tv.notube.crawler.requester.DefaultRequester;
import tv.notube.crawler.requester.Requester;
import tv.notube.usermanager.MockUserManager;
import tv.notube.usermanager.UserManager;

/**
 * 
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class ProductionCrawlerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ParallelCrawlerImpl.class);
        bind(UserManager.class).to(MockUserManager.class);
        bind(Requester.class).to(DefaultRequester.class);
    }
}