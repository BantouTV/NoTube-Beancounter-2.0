package tv.notube.platform.responses;

import tv.notube.platform.PlatformResponse;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Defines the result of a processing.
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
@Produces(MediaType.APPLICATION_JSON)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class PlatformResponseString extends PlatformResponse<String> {

    private String string;

    public PlatformResponseString(){}

    public PlatformResponseString(Status s, String m) {
        super(s, m);
    }

    public PlatformResponseString(Status s, String m, String str) {
        super(s, m);
        string = str;
    }


    public String getObject() {
        return string;
    }

}