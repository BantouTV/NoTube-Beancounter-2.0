package io.beancounter.platform;

import io.beancounter.platform.responses.StringPlatformResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public abstract class JsonService extends Service {

    protected static final Logger LOGGER = LoggerFactory.getLogger(JsonService.class);

    public static Response error(
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

    public static Response error(String message) {
        Response.ResponseBuilder rb = Response.serverError();
        rb.entity(
                new StringPlatformResponse(
                        StringPlatformResponse.Status.NOK,
                        message
                )
        );
        return rb.build();
    }

    public static Response success(String message) {
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new StringPlatformResponse(
                        StringPlatformResponse.Status.OK,
                        message
                )
        );
        return rb.build();
    }

    public static Response success(String message, String object) {
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new StringPlatformResponse(
                        StringPlatformResponse.Status.OK,
                        message,
                        object
                )
        );
        return rb.build();
    }
}
