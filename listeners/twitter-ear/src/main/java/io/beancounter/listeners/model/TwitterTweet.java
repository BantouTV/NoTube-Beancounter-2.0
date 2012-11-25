package io.beancounter.listeners.model;

import io.beancounter.commons.model.activity.Coordinates;
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

    private Coordinates coords;

    List<String> mentionedUser = new ArrayList<String>();

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

    public void setCoords(Coordinates coords) {
        this.coords = coords;
    }

    public Coordinates getCoords() {
        return coords;
    }

    public void addMentionedUser(String screenName) {
        mentionedUser.add(screenName);
    }

    public List<String> getMentionedUser() {
        return mentionedUser;
    }

    public void setMentionedUser(List<String> mentionedUser) {
        this.mentionedUser = mentionedUser;
    }
}
