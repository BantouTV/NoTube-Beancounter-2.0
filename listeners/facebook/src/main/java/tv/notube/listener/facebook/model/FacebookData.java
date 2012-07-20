package tv.notube.listener.facebook.model;

import com.restfb.Facebook;
import com.restfb.types.NamedFacebookType;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class FacebookData extends NamedFacebookType {

    private String id;

    @Facebook
    private String category;

    @Facebook("created_time")
    private String createdTime;

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

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return super.getId();
    }
}