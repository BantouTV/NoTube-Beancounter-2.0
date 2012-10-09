package io.beancounter.platform;

import com.google.inject.Inject;
import io.beancounter.platform.validation.Validations;
import org.codehaus.jackson.map.ObjectMapper;
import io.beancounter.applications.ApplicationsManager;
import io.beancounter.filter.manager.FilterManager;
import io.beancounter.filter.manager.FilterManagerException;
import io.beancounter.filter.model.Filter;
import io.beancounter.filter.model.pattern.ActivityPattern;
import io.beancounter.platform.responses.FilterPlatformResponse;
import io.beancounter.platform.responses.StringsPlatformResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import static io.beancounter.applications.ApplicationsManager.Action.*;
import static io.beancounter.applications.ApplicationsManager.Object.FILTER;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Path("rest/filters")
@Produces(MediaType.APPLICATION_JSON)
public class FilterService extends JsonService {

    private ApplicationsManager applicationsManager;

    private FilterManager filterManager;

    @Inject
    public FilterService(ApplicationsManager applicationsManager, FilterManager filterManager) {
        this.applicationsManager = applicationsManager;
        this.filterManager = filterManager;
    }

    @POST
    @Path("/register/{name}")
    public Response register(
            @PathParam("name") String name,
            @FormParam("description") String description,
            @FormParam("pattern") String patternJson,
            @FormParam("queue") Set<String> queues,
            @QueryParam("apikey") String apiKey
    ) {
        try {
            Validations.checkNotEmpty(name, "Filter name must not be empty");
            Validations.checkNotEmpty(description, "Missing description parameter");
            Validations.checkNotEmpty(patternJson, "Missing filter pattern JSON");
            Validations.checkNotEmpty(queues, "You must specify at least one queue");
            Validations.validateApiKey(apiKey, applicationsManager, CREATE, FILTER);
        } catch (Exception ex) {
            return error(ex.getMessage());
        }

        ActivityPattern pattern;
        try {
            pattern = parse(patternJson);
        } catch (IOException e) {
            return error(e, "Error: cannot parse your input json");
        }

        String actualName;
        try {
            actualName = filterManager.register(name, description, queues, pattern);
        } catch (FilterManagerException e) {
            return error(e, "Error while registering filter [" + name + "]");
        }

        return success("filter [" + name + "] successfully registered", actualName);
    }

    @GET
    @Path("/{name}")
    public Response get(
            @PathParam("name") String name,
            @QueryParam("apikey") String apiKey
    ) {
        Filter filter;
        try {
            Validations.checkNotEmpty(name, "Filter name must not be empty");
            Validations.validateApiKey(apiKey, applicationsManager, RETRIEVE, FILTER);
            filter = filterManager.get(name);
            Validations.checkNotNull(filter, "Filter [" + name + "] does not exist");
        } catch (Exception ex) {
            return error(ex.getMessage());
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
            Validations.checkNotEmpty(name, "Filter name must not be empty");
            Validations.validateApiKey(apikey, applicationsManager, DELETE, FILTER);
            filterManager.delete(name);
        } catch (Exception ex) {
            return error(ex.getMessage());
        }

        return success("filter with name [" + name + "] deleted", name);
    }

    @GET
    @Path("/{name}/start")
    public Response start(
            @PathParam("name") String name,
            @QueryParam("apikey") String apikey
    ) {
        try {
            Validations.checkNotEmpty(name, "Filter name must not be empty");
            Validations.validateApiKey(apikey, applicationsManager, UPDATE, FILTER);
        } catch (Exception ex) {
            return error(ex.getMessage());
        }

        try {
            filterManager.start(name);
        } catch (FilterManagerException e) {
            return error(e, "Error while starting filter with name [" + name + "]");
        }

        return success("filter with name [" + name + "] started", name);
    }

    @GET
    @Path("/list/all")
    public Response filters(@QueryParam("apikey") String apikey) {
        try {
            Validations.validateApiKey(apikey, applicationsManager, RETRIEVE, FILTER);
        } catch (Exception ex) {
            return error(ex.getMessage());
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
            Validations.checkNotEmpty(name, "Filter name must not be empty");
            Validations.validateApiKey(apikey, applicationsManager, UPDATE, FILTER);
        } catch (Exception ex) {
            return error(ex.getMessage());
        }

        try {
            filterManager.stop(name);
        } catch (FilterManagerException e) {
            return error(e, "Error while stopping filter with name [" + name +  "]");
        }

        return success("filter with name [" + name + "] stopped", name);
    }

    private ActivityPattern parse(String patternJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(patternJson, ActivityPattern.class);
    }
}