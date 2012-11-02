package io.beancounter.commons.model.activity.rai;

import java.net.URL;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class RaiTvObject extends io.beancounter.commons.model.activity.Object {

    private String onEvent;

    public RaiTvObject() {
        super();
    }

    public RaiTvObject(URL url) {
        super(url);
    }

    public String getOnEvent() {
        return onEvent;
    }

    public void setOnEvent(String onEvent) {
        this.onEvent = onEvent;
    }

    @Override
    public String toString() {
        return "RaiTvObject{" +
                "onEvent='" + onEvent + '\'' +
                '}';
    }
}
