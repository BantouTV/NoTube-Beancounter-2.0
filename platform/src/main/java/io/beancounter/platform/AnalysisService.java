package io.beancounter.platform;

import com.google.inject.Inject;
import io.beancounter.analyses.Analyses;
import io.beancounter.analyses.AnalysesException;
import io.beancounter.applications.ApplicationsManager;
import io.beancounter.commons.model.AnalysisResult;
import io.beancounter.platform.responses.AnalysisResultPlatformResponse;
import io.beancounter.platform.responses.StringPlatformResponse;
import io.beancounter.platform.validation.Validations;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static io.beancounter.applications.ApplicationsManager.Action.RETRIEVE;
import static io.beancounter.applications.ApplicationsManager.Object.FILTER;

/**
 * This service implements all the <i>REST</i> APIs needed to manage
 * realtime analyses.
 *
 * @see {@link io.beancounter.analyses.Analyses}
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Path("rest/analysis")
@Produces(MediaType.APPLICATION_JSON)
public class AnalysisService extends JsonService {

    private ApplicationsManager applicationsManager;

    private Analyses analyses;

    @Inject
    public AnalysisService(
            final ApplicationsManager am,
            final Analyses analyses
    ) {
        applicationsManager = am;
        this.analyses = analyses;
    }

    @GET
    @Path("/{analysisId}/restart")
    public Response restart(
            @PathParam("analysisId") String analysisId,
            @QueryParam("apikey") String apikey
    ) {
        throw new UnsupportedOperationException("niy");
    }

    @GET
    @Path("/{analysisId}/result")
    public Response result(
            @PathParam("analysisId") String analysisId,
            @QueryParam("apikey") String apikey
    ) {
        try {
            Validations.validateApiKey(apikey, applicationsManager, RETRIEVE, FILTER);
        } catch (Exception ex) {
            return error(ex.getMessage());
        }

        AnalysisResult analysisResult;
        try {
            analysisResult = analyses.lookup(analysisId);
        } catch (AnalysesException e) {
            return error(e, "error while getting result for analysis [" + analysisId + "]");
        }

        if (analysisResult == null) {
            return error("result for analysis [" + analysisId + "] not found");
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new AnalysisResultPlatformResponse(
                        AnalysisResultPlatformResponse.Status.OK,
                        "analysis [" + analysisId + "] result found",
                        analysisResult
                )
        );
        return rb.build();
    }

    @GET
    @Path("/list/all")
    public Response analyses(@QueryParam("apikey") String apikey) {
        throw new UnsupportedOperationException("niy");
    }

}
