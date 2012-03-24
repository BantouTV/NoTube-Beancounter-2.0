package tv.notube.crawler.requester.request.facebook;

import org.joda.time.DateTime;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FacebookLike {

    public FacebookLike(String name, String category, long id, DateTime createdAt) {
        this.name = name;
        this.category = category;
        this.id = id;
        this.createdAt = createdAt;
    }

    private String name;

    private String category;

    private long id;

    private DateTime createdAt;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(DateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FacebookLike that = (FacebookLike) o;

        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "FacebookLike{" +
                "name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", id=" + id +
                ", createdAt=" + createdAt +
                '}';
    }
}
