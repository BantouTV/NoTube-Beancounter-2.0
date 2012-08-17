package io.beancounter.commons.model.activity;

import java.net.URL;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Article extends Object {

    private String summary;

    private String content;

    private URL permalink;

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public URL getPermalink() {
        return permalink;
    }

    public void setPermalink(URL permalink) {
        this.permalink = permalink;
    }

    @Override
    public String toString() {
        return "Article{" +
                "summary='" + summary + '\'' +
                ", content='" + content + '\'' +
                ", permalink=" + permalink +
                '}';
    }
}
