package tv.notube.crawler;

import tv.notube.crawler.requester.DefaultRequester;
import tv.notube.usermanager.DefaultUserManagerFactory;
import tv.notube.usermanager.UserManager;
import tv.notube.usermanager.UserManagerFactoryException;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class DefaultCrawlerFactory implements CrawlerFactory {

    private static CrawlerFactory instance;

    public static synchronized CrawlerFactory getInstance() {
        if (instance == null) {
            instance = new DefaultCrawlerFactory();
        }
        return instance;
    }

    private DefaultCrawlerFactory() {
        UserManager um;
        try {
            um = DefaultUserManagerFactory.getInstance().build();
        } catch (UserManagerFactoryException e) {
            final String errMsg = "Error while build user manager";
            throw new RuntimeException(errMsg, e);
        }
        crawler = new ParallelCrawlerImpl(um, new DefaultRequester());
    }

    private Crawler crawler;

    public Crawler build() throws CrawlerFactoryException {
        return crawler;
    }

}
