package io.beancounter.commons.model.activity;

import io.beancounter.commons.model.activity.rai.RaiTvObject;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import io.beancounter.commons.model.activity.facebook.Like;
import io.beancounter.commons.model.activity.rai.Comment;
import io.beancounter.commons.model.activity.rai.ContentItem;
import io.beancounter.commons.model.activity.rai.TVEvent;
import io.beancounter.commons.tests.annotations.Random;

import java.io.Serializable;
import java.net.URL;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Object.class, name = "OBJECT"),
        @JsonSubTypes.Type(value = Tweet.class, name = "TWEET"),
        @JsonSubTypes.Type(value = Event.class, name = "EVENT"),
        @JsonSubTypes.Type(value = Article.class, name = "ARTICLE"),
        @JsonSubTypes.Type(value = Place.class, name = "PLACE"),
        @JsonSubTypes.Type(value = Song.class, name = "SONG"),
        @JsonSubTypes.Type(value = ContentItem.class, name = "RAI-CONTENT-ITEM"),
        @JsonSubTypes.Type(value = TVEvent.class, name = "RAI-TV-EVENT"),
        @JsonSubTypes.Type(value = Comment.class, name = "RAI-TV-COMMENT"),
        @JsonSubTypes.Type(value = RaiTvObject.class, name = "RAI-TV-OBJECT"),
        @JsonSubTypes.Type(value = Like.class, name = "FB-LIKE")
})
public class Object implements Serializable {

    private static final long serialVersionUID = 399673611235L;

    private URL url;

    private String name;

    private String description;

    public Object() {
        super();
    }

    @Random( names = { "url" } )
    public Object(URL url) {
        super();
        this.url = url;
    }

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
        if (!(o instanceof io.beancounter.commons.model.activity.Object)) return false;

        io.beancounter.commons.model.activity.Object object =
                (io.beancounter.commons.model.activity.Object)  o;

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
