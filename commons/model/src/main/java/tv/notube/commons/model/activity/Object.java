package tv.notube.commons.model.activity;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.net.URL;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Object implements Serializable {

    private static final long serialVersionUID = 399673611235L;

    @Expose
    private URL url;

    @Expose
    private String name;

    @Expose
    private String description;

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) return true;
        if (!(o instanceof java.lang.Object)) return false;

        tv.notube.commons.model.activity.Object object =
                (tv.notube.commons.model.activity.Object)  o;

        if (url != null ? !url.equals(object.url) : object.url != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Object{" +
                "url=" + url +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
