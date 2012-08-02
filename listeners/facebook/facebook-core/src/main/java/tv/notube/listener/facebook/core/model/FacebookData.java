package tv.notube.listener.facebook.core.model;

import com.restfb.Facebook;
import com.restfb.types.NamedFacebookType;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class FacebookData extends NamedFacebookType {

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
}