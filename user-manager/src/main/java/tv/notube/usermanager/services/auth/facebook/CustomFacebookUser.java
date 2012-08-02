package tv.notube.usermanager.services.auth.facebook;

import com.restfb.Facebook;
import com.restfb.json.JsonObject;
import com.restfb.json.JsonTokener;

import java.io.IOException;

/**
 * This class is intended to wrap user data from <i>Facebook Response</i>.
 *
 * @see <a href="https://developers.facebook.com/docs/reference/api/user/">Facebook field list</a>.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class CustomFacebookUser {

    @Facebook
    private String id;

    @Facebook("first_name")
    private String firstName;

    @Facebook
    private String picture;

    @Facebook("last_name")
    private String lastName;

    @Facebook
    private String gender;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getPicture() {
        JsonObject jsonObject = new JsonObject(new JsonTokener(picture));
        return jsonObject.getJsonObject("data").getString("url");
    }

    public void setPicture(String picture) throws IOException {
        this.picture = picture;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "CustomFacebookUser{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", picture='" + picture + '\'' +
                ", lastName='" + lastName + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }
}