package io.beancounter.platform.responses;

import io.beancounter.platform.PlatformResponse;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
@XmlRootElement
public class StringPlatformResponse extends PlatformResponse<String> {

    private String string;

    public StringPlatformResponse() {}

    public StringPlatformResponse(Status status, String message) {
        super(status, message);
    }

    public StringPlatformResponse(Status status, String message, String str) {
        super(status, message);
        this.string = str;
    }

    @XmlElement
    public String getObject() {
        return string;
    }

    public void setObject(String object) {
        this.string = object;
    }

}