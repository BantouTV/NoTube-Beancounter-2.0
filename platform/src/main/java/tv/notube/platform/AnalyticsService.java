package tv.notube.platform;

import com.google.inject.Inject;
import tv.notube.analytics.Analyzer;
import tv.notube.analytics.AnalyzerException;
import tv.notube.analytics.analysis.AnalysisResult;
import tv.notube.applications.ApplicationsManager;
import tv.notube.applications.ApplicationsManagerException;
import tv.notube.commons.configuration.analytics.AnalysisDescription;
import tv.notube.commons.configuration.analytics.MethodDescription;
import tv.notube.platform.responses.PlatformResponseAnalyses;
import tv.notube.platform.responses.PlatformResponseAnalysis;
import tv.notube.platform.responses.PlatformResponseString;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.lang.Object;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Path("/analytics")
@Produces(MediaType.APPLICATION_JSON)
public class AnalyticsService extends JsonService {

    private ApplicationsManager applicationsManager;

    private Analyzer analyzer;

    @Inject
    public AnalyticsService(final ApplicationsManager am, final Analyzer an) {
        this.applicationsManager = am;
        this.analyzer = an;
    }

    @GET
    @Path("/analyses")
    public Response getAvailableAnalyses(
            @QueryParam("apikey") String apiKey
    ) {
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(apiKey);
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authorizing your application");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new PlatformResponseString(
                    PlatformResponseString.Status.NOK,
                    "Your application is not authorized.Sorry.")
            );
            return rb.build();
        }

        AnalysisDescription[] analysisDescriptions;
        try {
            analysisDescriptions = analyzer.getRegisteredAnalysis();
        } catch (AnalyzerException e) {
            return error(e, "Error while getting registered analysis");
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new PlatformResponseAnalyses(
                PlatformResponseAnalyses.Status.OK,
                "analysis found",
                analysisDescriptions)
        );
        return rb.build();
    }

    @GET
    @Path("/analysis/{name}")
    public Response getAnalysisDescription(
            @PathParam("name") String name,
            @QueryParam("apikey") String apiKey
    ) {

        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(apiKey);
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authorizing your application");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new PlatformResponseString(
                    PlatformResponseString.Status.NOK,
                    "Your application is not authorized.Sorry.")
            );
            return rb.build();
        }
        AnalysisDescription analysisDescription;
        try {
            analysisDescription = analyzer.getAnalysisDescription(name);
        } catch (AnalyzerException e) {
            return error(e, "Error while getting registered analysis");
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new PlatformResponseAnalysis(
                PlatformResponseAnalysis.Status.OK,
                "analysis description",
                analysisDescription)
        );
        return rb.build();
    }


    @GET
    @Path("/analysis/{name}/{user}/{method}")
    public Response getAnalysisResult(
            @PathParam("name") String name,
            @PathParam("user") String user,
            @PathParam("method") String methodName,
            @QueryParam("apikey") String apiKey,
            @Context UriInfo uriInfo
    ) {
        boolean isAuth;
        try {
            isAuth = applicationsManager.isAuthorized(apiKey);
        } catch (ApplicationsManagerException e) {
            return error(e, "Error while authorizing your application");
        }
        if (!isAuth) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new PlatformResponseString(
                    PlatformResponseString.Status.NOK,
                    "Your application is not authorized.Sorry.")
            );
            return rb.build();
        }
        AnalysisDescription analysisDescription;
        try {
            analysisDescription = analyzer.getAnalysisDescription(name);
        } catch (AnalyzerException e) {
            return error(e, "Error while getting analysis description");
        }
        AnalysisResult analysisResult;
        try {
            analysisResult = analyzer.getResult(name, user);
        } catch (AnalyzerException e) {
            return error(e, "Error while getting registered analysis");
        }
        if (analysisResult == null) {
            Response.ResponseBuilder rb = Response.serverError();
            rb.entity(new PlatformResponseString(
                    PlatformResponseString.Status.NOK,
                    "analysis without result")
            );
            return rb.build();
        }
        String resultClassName = analysisDescription.getResultClassName();
        MethodDescription mds[] =
                analysisDescription.getMethodDescriptions(methodName);

        String params[] = getParams(uriInfo.getQueryParameters().get("param"));

        String result;
        try {
            result = getResult(
                    analysisResult,
                    resultClassName,
                    mds,
                    params
            );
            // TODO customize errors
        } catch (ClassNotFoundException e) {
            return error(e, "Analyis result not found");
        } catch (InvocationTargetException e) {
            return error(e, "Analyis invocating analysis result");
        } catch (NoSuchMethodException e) {
            return error(e, "Analyis method not found");
        } catch (InstantiationException e) {
            return error(e, "Error instantiating analysis result");
        } catch (IllegalAccessException e) {
            return error(e, "Error accessing analysis result");
        } catch (ServiceException e) {
            return error(e, "Analysis result method not found");
        }
        Response.ResponseBuilder rb = Response.ok();
        rb.entity(new PlatformResponseString(PlatformResponseString.Status.OK, "analysis result", result));
        return rb.build();
    }

    private String[] getParams(List<String> values) {
        if (values == null)
            return new String[0];
        return values.toArray(new String[values.size()]);
    }

    private String getResult(
            AnalysisResult analysisResult,
            String resultClassName,
            MethodDescription mds[],
            String params[]
    ) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ServiceException {
        Class analysisResultClass = Class.forName(resultClassName);
        Object typedResult;
        try {
            typedResult = analysisResultClass.cast(analysisResult);
        } catch (ClassCastException e) {
            throw new ServiceException(
                    "[" + analysisResult + "] must extend AnalysisResult",
                    e
            );
        }
        return invokeMethod(typedResult, mds, params);
    }

    private String invokeMethod(
            Object typedResult,
            MethodDescription[] mds,
            String[] params
    ) throws ServiceException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Method method;
        for (MethodDescription md : mds) {
            if (params.length == md.getParameterTypes().length) {
                Class<? extends Object> methodSignature[] =
                        getMethodSignature(md.getParameterTypes());
                method = typedResult.getClass().getMethod(
                        md.getName(),
                        methodSignature
                );
                return String.valueOf(method.invoke(
                        typedResult,
                        getActualSignature(md.getParameterTypes(), params)
                ));
            }
        }
        throw new ServiceException("method not found");
    }

    private Object[] getActualSignature(
            String[] parameterTypes,
            String[] params
    ) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Object objs[] = new Object[params.length];
        Class<? extends Object>[] signature = getMethodSignature(parameterTypes);
        int i = 0;
        for (Class<? extends Object> c : signature) {
            objs[i] = c.getConstructor(String.class).newInstance(params[i]);
            i++;
        }
        return objs;
    }

    private Class<? extends Object>[] getMethodSignature(
            String[] parameterTypes) {
        List<Class<? extends Object>> classes = new ArrayList<Class<? extends Object>>();
        for (String parameterType : parameterTypes) {
            try {
                classes.add(Class.forName(parameterType));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(
                        "Parameter with type: '" + parameterType + "'",
                        e
                );
            }
        }
        return classes.toArray(new Class[classes.size()]);
    }



}
