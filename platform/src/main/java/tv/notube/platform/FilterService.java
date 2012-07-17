package tv.notube.platform;

import com.google.inject.Inject;
import org.codehaus.jackson.map.ObjectMapper;
import tv.notube.applications.ApplicationsManager;
import tv.notube.applications.ApplicationsManagerException;
import tv.notube.filter.FilterManager;
import tv.notube.filter.FilterManagerException;
import tv.notube.filter.model.Filter;
import tv.notube.filter.model.pattern.ActivityPattern;
import tv.notube.platform.responses.FilterPlatformResponse;
import tv.notube.platform.responses.StringsPlatformResponse;
import tv.notube.platform.responses.StringPlatformResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Path("rest/filters")
@Produces(MediaType.APPLICATION_JSON)
public class FilterService extends JsonService {

    @Inject
    private ApplicationsManager applicationsManager;

    @Inject
    private FilterManager filterManager;

    @Inject
    public FilterService() {}

    @POST
    @Path("/register/{name}")
    public Response register(
            @PathParam("name") String name,
            @FormParam("description") String description,
            @FormParam("pattern") String patternJson,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            check(
                    this.getClass(),
                    "register",
                    name,
                    description,
                    patternJson,
                    apiKey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        try {
            UUID.fromString(apiKey);
        } catch (IllegalArgumentException e) {
            return error(e, "Your apikey is not well formed");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(
                    UUID.fromString(apiKey),
                    ApplicationsManager.Action.CREATE,
                    ApplicationsManager.Object.FILTER
            );
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authorizing your application");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "application with key [" + apiKey + "] is not authorized")
            );
            return rb.build();
        }
        ActivityPattern pattern;
        try {
            pattern = parse(patternJson);
        } catch (IOException e) {
            final String errMsg = "Error: cannot parse your input json";
            return error(e, errMsg);
        }
        String actualName;
        try {
            actualName = filterManager.register(name, description, pattern);
        } catch (FilterManagerException e) {
            final String errMsg = "Error while [" + name + "] filter";
            return error(e, errMsg);
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new StringPlatformResponse(
                StringPlatformResponse.Status.OK,
                "filter [" + name + "] successfully registered",
                actualName)
        );
        return rb.build();
    }

    @GET
    @Path("/{name}")
    public Response get(
            @PathParam("name") String name,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            check(
                    this.getClass(),
                    "get",
                    name,
                    apiKey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(
                    UUID.fromString(apiKey),
                    ApplicationsManager.Action.CREATE,
                    ApplicationsManager.Object.FILTER
            );
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authorizing your application");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "application with key [" + apiKey + "] is not authorized")
            );
            return rb.build();
        }
        Filter filter;
        try {
            filter = filterManager.get(name);
        } catch (FilterManagerException e) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "Filter [" + name + "] does not exist")
            );
            return rb.build();
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new FilterPlatformResponse(
                        FilterPlatformResponse.Status.OK,
                        "filter with name [" + name + "] found",
                        filter
                )
        );
        return rb.build();
    }

    @DELETE
    @Path("/{name}")
    public Response delete(
            @PathParam("name") String name,
            @QueryParam("apikey") String apikey
    ) {
        try {
            check(
                    this.getClass(),
                    "get",
                    name,
                    apikey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(
                    UUID.fromString(apikey),
                    ApplicationsManager.Action.DELETE,
                    ApplicationsManager.Object.FILTER
            );
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authorizing your application");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "application with key [" + apikey + "] is not authorized")
            );
            return rb.build();
        }
        try {
            filterManager.delete(name);
        } catch (FilterManagerException e) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "Filter [" + name + "] does not exist")
            );
            return rb.build();
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new StringPlatformResponse(
                        StringPlatformResponse.Status.OK,
                        "filter with name [" + name + "] deleted",
                        name
                )
        );
        return rb.build();
    }

    @GET
    @Path("/{name}/start")
    public Response start(
            @PathParam("name") String name,
            @QueryParam("apikey") String apikey
    ) {
        try {
            check(
                    this.getClass(),
                    "start",
                    name,
                    apikey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(
                    UUID.fromString(apikey),
                    ApplicationsManager.Action.UPDATE,
                    ApplicationsManager.Object.FILTER
            );
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authorizing your application");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "application with key [" + apikey + "] is not authorized")
            );
            return rb.build();
        }
        try {
            filterManager.start(name);
        } catch (FilterManagerException e) {
            return error(e, "Error while starting filter with name [" + name + "]");
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new StringPlatformResponse(
                        StringPlatformResponse.Status.OK,
                        "filter with name [" + name + "] started",
                        name
                )
        );
        return rb.build();
    }

    @GET
    @Path("/list/all")
    public Response filters(@QueryParam("apikey") String apikey) {
        try {
            check(
                    this.getClass(),
                    "filters",
                    apikey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(
                    UUID.fromString(apikey),
                    ApplicationsManager.Action.RETRIEVE,
                    ApplicationsManager.Object.FILTER
            );
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authorizing your application");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "application with key [" + apikey + "] is not authorized")
            );
            return rb.build();
        }
        Collection<String> filters;
        try {
            filters = filterManager.getRegisteredFilters();
        } catch (FilterManagerException e) {
            return error(e, "Error while getting registered filters");
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new StringsPlatformResponse(
                        StringsPlatformResponse.Status.OK,
                        "[" + filters.size() + "] registered filters found",
                        filters
                    )
        );
        return rb.build();
    }

    @GET
    @Path("/{name}/stop")
    public Response stop(
            @PathParam("name") String name,
            @QueryParam("apikey") String apikey
    ) {
        try {
            check(
                    this.getClass(),
                    "start",
                    name,
                    apikey
            );
        } catch (ServiceException e) {
            return error(e, "Error while checking parameters");
        }
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(
                    UUID.fromString(apikey),
                    ApplicationsManager.Action.UPDATE,
                    ApplicationsManager.Object.FILTER
            );
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authorizing your application");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new StringPlatformResponse(
                    StringPlatformResponse.Status.NOK,
                    "application with key [" + apikey + "] is not authorized")
            );
            return rb.build();
        }
        try {
            filterManager.stop(name);
        } catch (FilterManagerException e) {
            return error(e, "Error while stopping filter with name [" + name +  "]");
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new StringPlatformResponse(
                        StringPlatformResponse.Status.OK,
                        "filter with name [" + name + "] stopped",
                        name
                )
        );
        return rb.build();
    }

    private ActivityPattern parse(String patternJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(patternJson, ActivityPattern.class);
    }

}