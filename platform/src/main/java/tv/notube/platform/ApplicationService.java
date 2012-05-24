package tv.notube.platform;

import com.google.inject.Inject;
import tv.notube.applications.Application;
import tv.notube.applications.ApplicationsManager;
import tv.notube.applications.ApplicationsManagerException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Path("/application")
@Produces(MediaType.APPLICATION_JSON)
public class ApplicationService {

    private ApplicationsManager applicationsManager;

    @Inject
    public ApplicationService(final ApplicationsManager am) {
        this.applicationsManager = am;
    }

    @POST
    @Path("/register")
    public PlatformResponseString register(
            @FormParam("name") String name,
            @FormParam("description") String description,
            @FormParam("email") String email,
            @FormParam("oauthCallback") String oauth
    ) {
        Application application = new Application(name, description, email);
        try {
            application.setOAuthCallback(new URL(oauth));
        } catch (MalformedURLException e) {
            throw new RuntimeException(
                    "Provided URL '" + oauth + "' is not well formed",
                    e
            );
        }
        String apiKey;
        try {
            apiKey = applicationsManager.registerApplication(application);
        } catch (ApplicationsManagerException e) {
            throw new RuntimeException(
                    "Error while registering application '" + name + "'",
                    e
            );
        }
        return new PlatformResponseString(
                PlatformResponseString.Status.OK,
                "Application '" + name + "' successfully registered",
                apiKey
        );
    }

    @DELETE
    @Path("/{name}")
    public PlatformResponseString deregisterApplication(
            @PathParam("name") String name
    ) {
        try {
            applicationsManager.deregisterApplication(name);
        } catch (ApplicationsManagerException e) {
            throw new RuntimeException(
                    "Error while deregistering application '" + name + "'",
                    e
            );
        }
        return new PlatformResponseString(
                PlatformResponseString.Status.OK,
                "Application '" + name + "' successfully removed"
        );
    }

}
