package io.beancounter.commons.model;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * It represent a {@link User} category of interest.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Category extends Topic<Category> {

    private Collection<URL> urls = new ArrayList<URL>();

    public Category() {
    }

    public Category(String label, URI resource) {
        super(resource, label);
    }

    public Collection<URL> getUrls() {
        return urls;
    }

    public void setUrls(Collection<URL> urls) {
        this.urls = urls;
    }

    public void addUrl(URL url) {
        urls.add(url);
    }

    @Override
    public String toString() {
        return "Category{" +
                "urls=" + urls +
                "} " + super.toString();
    }

    @Override
    public Category merge(Category nu, Category old, int threshold) {
        // if the activities of the old one are above the threshold, drop the exceeding ones
        if (old.getUrls().size() > threshold) {
            List<URL> oldActivities = new ArrayList<URL>(old.getUrls());
            for (int i = 0; i < oldActivities.size() - threshold; i++) {
                oldActivities.remove(i);
            }
            old.setUrls(oldActivities);
        }
        for (URL url : nu.getUrls()) {
            old.addUrl(url);
        }
        old.setWeight(old.getWeight() + nu.getWeight());
        return old;
    }

}
