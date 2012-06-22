package tv.notube.platform;

import com.google.inject.Inject;
import tv.notube.applications.ApplicationsManager;
import tv.notube.applications.ApplicationsManagerException;
import tv.notube.platform.responses.StringPlatformResponse;
import tv.notube.platform.responses.UUIDPlatformResponse;

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
public class ApplicationService {

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
            throw new RuntimeException(
                    "Provided URL '" + oauth + "' is not well formed",
                    e
            );
        }
        UUID apiKey;
        try {
            apiKey = applicationsManager.registerApplication(name, description, email, oauthUrl);
        } catch (ApplicationsManagerException e) {
            throw new RuntimeException(
                    "Error while registering application '" + name + "'",
                    e
            );
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
            applicationsManager.deregisterApplication(
                    UUID.fromString(apiKey)
            );
        } catch (ApplicationsManagerException e) {
            throw new RuntimeException(
                    "Error while deregistering application with api key [" + apiKey + "]",
                    e
            );
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new StringPlatformResponse(
                StringPlatformResponse.Status.OK,
                "Application with api key'" + apiKey + "' successfully removed")
        );
        return rb.build();
    }

}
