package tv.notube.platform;

import com.google.inject.Inject;
import tv.notube.platform.responses.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Path("rest/alive")
@Produces(MediaType.APPLICATION_JSON)
public class AliveService extends JsonService {

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

}