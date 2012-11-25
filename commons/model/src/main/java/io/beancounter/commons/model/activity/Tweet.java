package io.beancounter.commons.model.activity;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Tweet extends io.beancounter.commons.model.activity.Object {

    private String text;

    private Set<String> hashTags = new HashSet<String>();

    private List<URL> urls = new ArrayList<URL>();

    private Coordinates geo;

    private Set<String> mentionedUsers = new HashSet<String>();

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Set<String> getHashTags() {
        return hashTags;
    }

    public void setHashTags(Set<String> hashTags) {
        this.hashTags = hashTags;
    }

    public List<URL> getUrls() {
        return urls;
    }

    public void setUrls(List<URL> urls) {
        this.urls = urls;
    }

    public boolean addHashTag(String s) {
        return hashTags.add(s);
    }

    public boolean addUrl(URL url) {
        return urls.add(url);
    }

    public Coordinates getGeo() {
        return geo;
    }

    public void setGeo(Coordinates geo) {
        this.geo = geo;
    }

    public Set<String> getMentionedUsers() {
        return mentionedUsers;
    }

    public void setMentionedUsers(Set<String> mentionedUsers) {
        this.mentionedUsers = mentionedUsers;
    }

    public void addMentionedUsers(String mentionedUser) {
        mentionedUsers.add(mentionedUser);
    }

}
