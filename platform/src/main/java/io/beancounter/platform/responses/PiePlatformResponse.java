package io.beancounter.platform.responses;

import io.beancounter.platform.PlatformResponse;

import java.util.List;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class PiePlatformResponse extends PlatformResponse<List<List<Object>>> {

    private List<List<Object>> object;

    public PiePlatformResponse() {}

    public PiePlatformResponse(Status s, String m, List<List<Object>> object) {
        super(s, m);
        this.object = object;
    }

    @Override
    public List<List<Object>> getObject() {
        return object;
    }
}