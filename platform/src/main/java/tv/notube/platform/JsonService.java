package tv.notube.platform;

import javax.ws.rs.core.Response;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class JsonService extends Service {

    protected Response error(
            Exception e,
            String message
    ) {
        Response.ResponseBuilder rb = Response.serverError();
        rb.entity(
                new PlatformResponseString(
                        PlatformResponseString.Status.NOK,
                        message,
                        e.getMessage()
                )
        );
        return rb.build();
    }

}
