package tv.notube.commons.tagdef;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import tv.notube.commons.tagdef.handler.TagDefResponseDeserializer;

import java.net.URL;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Def {

    private String text;

    private URL url;

    public Def(String text, URL url) {
        this.text = text;
        this.url = url;
    }

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
