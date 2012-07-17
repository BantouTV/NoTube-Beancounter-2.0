package tv.notube.filter.model.pattern;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class URLPattern implements Pattern<URL> {

    public static final URLPattern ANY = new URLPattern();

    private URL url;

    private URLPattern() {}

    public URLPattern(URL url) {
        this.url = url;
    }

    public URLPattern(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("[" + url + "] is ill-formed", e);
        }
    }

    @Override
    public boolean matches(URL url) {
        return this.equals(ANY) || this.url.equals(url);
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        URLPattern that = (URLPattern) o;

        if (url != null ? !url.equals(that.url) : that.url != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "URLPattern{" +
                "url=" + url +
                '}';
    }
}
