package io.beancounter.platform.responses;

import io.beancounter.applications.model.Application;
import io.beancounter.platform.PlatformResponse;

/**
 *
 * @author Enrico Candino ( enrico.candino @ gmail.com )
 */
public class ApplicationPlatformResponse extends PlatformResponse<Application> {

    private Application application;

    public ApplicationPlatformResponse() {}

    public ApplicationPlatformResponse(Status s, String m) {
        super(s, m);
    }

    public ApplicationPlatformResponse(Status s, String m, Application app) {
        super(s, m);
        this.application = app;
    }

    @Override
    public Application getObject() {
        return application;
    }

    public void setObject(Application application) {
        this.application = application;
    }
}
