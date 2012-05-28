package tv.notube.commons.model.activity;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Song extends Object {

    private String mbid;

    public void setMbid(String mbid) {
        this.mbid = mbid;
    }

    public String getMbid() {
        return mbid;
    }

    @Override
    public String toString() {
        return "Song{" +
                "mbid='" + mbid + '\'' +
                '}';
    }
}
