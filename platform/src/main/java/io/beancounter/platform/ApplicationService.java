package io.beancounter.platform;

import com.google.inject.Inject;
import io.beancounter.applications.ApplicationsManager;
import io.beancounter.applications.ApplicationsManagerException;
import io.beancounter.platform.responses.StringPlatformResponse;
import io.beancounter.platform.responses.UUIDPlatformResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Path("rest/application")
@Produces(MediaType.APPLICATION_JSON)
public class ApplicationService extends JsonService {

    private ApplicationsManager applicationsManager;

    @Inject
    public ApplicationService(final ApplicationsManager am) {
        this.applicationsManager = am;
    }

    @POST
    @Path("/register")
    public Response register(
            @FormParam("name") String name,
            @FormParam("description") String description,
            @FormParam("email") String email,
            @FormParam("oauthCallback") String oauth
    ) {
        URL oauthUrl;
        try {
            oauthUrl = new URL(oauth);
        } catch (MalformedURLException e) {
            return error(e, "Provided URL '" + oauth + "' is not well formed");
        }
        UUID apiKey;
        try {
            apiKey = applicationsManager.registerApplication(name, description, email, oauthUrl);
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while registering application [" + name + "]");
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new UUIDPlatformResponse(
                StringPlatformResponse.Status.OK,
                "Application '" + name + "' successfully registered",
                apiKey)
        );
        return rb.build();
    }

    @DELETE
    @Path("/{apiKey}")
    public Response deregisterApplication(
            @PathParam("apiKey") String apiKey
    ) {
        try {
            UUID.fromString(apiKey);
        } catch (IllegalArgumentException e) {
            return error(e, "Your apikey is not well formed");
        }
        boolean found;
        try {
            found = applicationsManager.deregisterApplication(
                    UUID.fromString(apiKey)
            );
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while deregistering application with api key [" + apiKey + "]");
        }
        if (!found) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "Application with api key '" + apiKey + "' not found")
            );
            return rb.build();
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new StringPlatformResponse(
                StringPlatformResponse.Status.OK,
                "Application with api key '" + apiKey + "' successfully removed")
        );
        return rb.build();
    }

}
