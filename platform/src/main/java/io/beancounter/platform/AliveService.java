package io.beancounter.platform;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import io.beancounter.platform.responses.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Path("rest/api")
@Produces(MediaType.APPLICATION_JSON)
public class AliveService extends JsonService {

    @Inject
    @Named("beancounter.app.version")
    String appVersion;

    @Inject
    public AliveService() {}

    @GET
    @Path("/check")
    public Response signUp() {
        long currentTime = System.currentTimeMillis();
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new StringPlatformResponse(
                UUIDPlatformResponse.Status.OK,
                "system up and running at",
                String.valueOf(currentTime))
        );
        return rb.build();
    }

    @GET
    @Path("/version")
    public Response version() {
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new StringPlatformResponse(
                UUIDPlatformResponse.Status.OK,
                "beancounter.io version",
                appVersion)
        );
        return rb.build();
    }

}