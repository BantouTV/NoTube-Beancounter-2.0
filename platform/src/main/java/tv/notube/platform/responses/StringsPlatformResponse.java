package tv.notube.platform.responses;

import tv.notube.platform.PlatformResponse;

import java.util.Collection;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class StringsPlatformResponse extends PlatformResponse<Collection<String>> {

    private Collection<String> strings;

    public StringsPlatformResponse(){}

    public StringsPlatformResponse(Status s, String m) {
        super(s, m);
    }

    public StringsPlatformResponse(Status s, String m, Collection<String> strings) {
        super(s, m);
        this.strings = strings;
    }

    public Collection<String> getObject() {
        return strings;
    }

    public void setObject(Collection<String> strings) {
        this.strings = strings;
    }
}