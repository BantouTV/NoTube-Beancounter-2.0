package tv.notube.platform;

import tv.notube.platform.responses.StringPlatformResponse;

import javax.ws.rs.core.Response;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public abstract class JsonService extends Service {

    protected Response error(
            Exception e,
            String message
    ) {
        Response.ResponseBuilder rb = Response.serverError();
        rb.entity(
                new StringPlatformResponse(
                        StringPlatformResponse.Status.NOK,
                        message,
                        e.getMessage()
                )
        );
        return rb.build();
    }

}
