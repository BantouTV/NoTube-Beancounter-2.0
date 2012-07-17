package tv.notube.platform.responses;

import tv.notube.filter.model.Filter;
import tv.notube.platform.PlatformResponse;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class FilterPlatformResponse extends PlatformResponse<Filter> {

    private Filter filter;

    public FilterPlatformResponse(){}

    public FilterPlatformResponse(Status s, String m) {
        super(s, m);
    }

    public FilterPlatformResponse(Status s, String m, Filter filter) {
        super(s, m);
        this.filter = filter;
    }

    public Filter getObject() {
        return filter;
    }

    public void setObject(Filter filter) {
        this.filter = filter;
    }
}