package tv.notube.crawler;

import tv.notube.usermanager.UserManager;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public abstract class AbstractCrawler implements Crawler {

    private UserManager userManager;

    public AbstractCrawler(UserManager userManager) {
        this.userManager = userManager;
    }

    public UserManager getUserManager() {
        return userManager;
    }

}
