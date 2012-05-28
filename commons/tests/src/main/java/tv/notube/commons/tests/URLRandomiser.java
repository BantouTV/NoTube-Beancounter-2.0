package tv.notube.commons.tests;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class URLRandomiser extends DefaultRandomiser<URL> {

    final private static String TEMPLATE = "%s.%s/%s";

    boolean www;

    boolean withQuery;

    private Random random = new Random();

    public URLRandomiser(String name, boolean withWww, boolean withQuery) {
        super(name);
        this.www = withWww;
        this.withQuery = withQuery;
    }

    public Class<URL> type() {
        return URL.class;
    }

    public URL getRandom() {
        return buildRandomURL();
    }

    private URL buildRandomURL() {
        String domain = getRandomString(10);
        String topLevel = getRandomString(3);
        String path = getRandomString(10);
        String url = String.format(TEMPLATE, domain, topLevel, path);
        if(www) {
            url = "www." + url;
        }
        if(withQuery) {
            String param = getRandomString(5);
            String value = getRandomString(10);
            url = url + String.format("?%s=%s", param, value);
        }
        try {
            return new URL("http://" + url);
        } catch (MalformedURLException e) {
            throw new RuntimeException("[" + url + "] is ill-formed", e);
        }
    }

    private String getRandomString(int length) {
        return new StringRandomiser("local", 1, length).getRandom();
    }

}
