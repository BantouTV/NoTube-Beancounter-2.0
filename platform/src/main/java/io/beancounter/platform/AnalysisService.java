package io.beancounter.platform;

import com.google.inject.Inject;
import io.beancounter.analyser.manager.AnalysisManager;
import io.beancounter.analyser.manager.AnalysisManagerException;
import io.beancounter.analyses.Analyses;
import io.beancounter.analyses.AnalysesException;
import io.beancounter.applications.ApplicationsManager;
import io.beancounter.commons.model.AnalysisResult;
import io.beancounter.platform.responses.AnalysisResultPlatformResponse;
import io.beancounter.platform.responses.StringsPlatformResponse;
import io.beancounter.platform.validation.Validations;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
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

    private AnalysisManager manager;

    @Inject
    public AnalysisService(
            final ApplicationsManager am,
            final Analyses analyses
    ) {
        applicationsManager = am;
        this.analyses = analyses;
    }

    @GET
    @Path("/{analysisId}/result")
    public Response result(
            @PathParam("analysisId") String analysisId,
            @QueryParam("apikey") String apikey
    ) {
        UUID analysisIdUUID;
        try {
            analysisIdUUID = UUID.fromString(analysisId);
            Validations.validateApiKey(apikey, applicationsManager, RETRIEVE, FILTER);
        } catch (Exception ex) {
            return error(ex.getMessage());
        }
        AnalysisResult analysisResult;
        try {
            analysisResult = analyses.lookup(analysisIdUUID);
        } catch (AnalysesException e) {
            return error(e, "");
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
        try {
            Validations.validateApiKey(apikey, applicationsManager, RETRIEVE, FILTER);
        } catch (Exception ex) {
            return error(ex.getMessage());
        }

        Collection<UUID> analyses;
        try {
            analyses = manager.getRegisteredAnalysis();
        } catch (AnalysisManagerException e) {
            return error(e, "Error while getting registered analyses");
        }

        Response.ResponseBuilder rb = Response.ok();
        rb.entity(
                new StringsPlatformResponse(
                        StringsPlatformResponse.Status.OK,
                        "[" + analyses.size() + "] registered analyses found",
                        toString(analyses)
                    )
        );
        return rb.build();
    }

    private Collection<String> toString(Collection<UUID> analyses) {
        Collection<String> analysesStr = new ArrayList<String>();
        analysesStr.add(analyses.toString());
        return analysesStr;
    }


}
