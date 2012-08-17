package io.beancounter.listener.model;

import org.joda.time.DateTime;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TwitterTweet {

    private DateTime createdAt;

    private String text;

    private String username;

    private List<URL> mentionedUrls = new ArrayList<URL>();

    private String name;

    private Set<String> hashTags = new HashSet<String>();

    private URL url;

    public void setCreatedAt(DateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setMentionedUrls(List<URL> mentionedUrls) {
        this.mentionedUrls = mentionedUrls;
    }

    public void addUrl(URL url) {
        this.mentionedUrls.add(url);
    }

    public void setName(String name) {
        this.name = name;
    }

    public DateTime getCreatedAt() {
        return createdAt;
    }

    public String getText() {
        return text;
    }

    public String getUsername() {
        return username;
    }

    public List<URL> getMentionedUrls() {
        return mentionedUrls;
    }

    public String getName() {
        return name;
    }

    public Set<String> getHashTags() {
        return hashTags;
    }

    public void setHashTags(Set<String> hashTags) {
        this.hashTags = hashTags;
    }

    public void addHashTag(String text) {
        this.hashTags.add(text);
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }
}
