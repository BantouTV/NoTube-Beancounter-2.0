package tv.notube.commons.tagdef;

import java.net.URL;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Def {

    private String text;

    private URL url;

    public String getText() {
        return text;
    }

    public URL getUrl() {
        return url;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setUrl(URL url) {
        this.url = url;
    }
}
