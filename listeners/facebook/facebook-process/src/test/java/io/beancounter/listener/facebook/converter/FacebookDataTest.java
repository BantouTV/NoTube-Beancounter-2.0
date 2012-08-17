package io.beancounter.listener.facebook.converter;

import io.beancounter.listener.facebook.core.model.FacebookData;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class FacebookDataTest extends FacebookData {

    private String id;

    private String name;

    private String category;

    private String createdTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }
}