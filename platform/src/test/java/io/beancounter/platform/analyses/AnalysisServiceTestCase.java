package io.beancounter.platform.analyses;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import io.beancounter.analyses.Analyses;
import io.beancounter.applications.ApplicationsManager;
import io.beancounter.applications.MockApplicationsManager;
import io.beancounter.commons.model.AnalysisResult;
import io.beancounter.platform.*;
import io.beancounter.platform.responses.AnalysisResultPlatformResponse;
import io.beancounter.platform.responses.StringPlatformResponse;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.joda.time.DateTime;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class AnalysisServiceTestCase extends AbstractJerseyTestCase {

    private static String APIKEY;

    private static Analyses analyses;

    protected AnalysisServiceTestCase() {
        super(9995);
    }

    @Override
    protected void startFrontendService() throws IOException {
        server = new GrizzlyWebServer(9995);
        ServletAdapter ga = new ServletAdapter();
        ga.addServletListener(AnalysisServiceTestConfig.class.getName());
        ga.setServletPath("/");
        ga.addFilter(new GuiceFilter(), "analysis", null);
        server.addGrizzlyAdapter(ga, null);
        server.start();
    }

    @BeforeTest
    public void registerApp() throws Exception {
        APIKEY = registerTestApplication().toString();
    }

    @AfterTest
    public void deregisterApp() throws IOException {
        HttpClient client = new HttpClient();
        String baseQuery = "application/" + APIKEY;
        DeleteMethod deleteMethod = new DeleteMethod(base_uri + baseQuery);
        client.executeMethod(deleteMethod);
    }

    @BeforeMethod
    private void resetMocks() throws Exception {
        reset(analyses);
    }

    @Test
    public void getResultWithAnExistentResultShouldBeOk() throws Exception {
        String analysisId = "analysis-name";
        String baseQuery = "analysis/%s/result?apikey=%s";
        String query = String.format(
                baseQuery,
                analysisId,
                APIKEY
        );

        when(analyses.lookup(analysisId)).thenReturn(getAnalysisResult(analysisId));

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        AnalysisResultPlatformResponse response = fromJson(
                responseBody,
                AnalysisResultPlatformResponse.class
        );
        assertEquals(response.getStatus(), AnalysisResultPlatformResponse.Status.OK);
        assertEquals(response.getMessage(), "analysis [" + analysisId + "] result found");
        AnalysisResult actual = response.getObject();
        assertEquals(actual.getAnalysisName(), analysisId);
        assertEquals(actual.getResults().size(), 3);
        assertEquals(actual.getResults().get("result.type"), "activity");
        assertEquals(actual.getResults().get("result.value"), "fake");
    }

    @Test
    public void getResultWithAnNonExistentResultShouldReturnError() throws Exception {
        String analysisId = "analysis-name";
        String baseQuery = "analysis/%s/result?apikey=%s";
        String query = String.format(
                baseQuery,
                analysisId,
                APIKEY
        );

        when(analyses.lookup(analysisId)).thenReturn(null);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(
                responseBody,
                StringPlatformResponse.class
        );
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "result for analysis [" + analysisId + "] not found");
    }

    private AnalysisResult getAnalysisResult(String analysisId) {
        AnalysisResult ar = new AnalysisResult(analysisId);
        ar.setLastUpdated(DateTime.now());
        ar.setValue("result.value", "fake");
        ar.setValue("result.type", "activity");
        ar.setValue("activity.id", UUID.randomUUID());
        return ar;
    }

    public static class AnalysisServiceTestConfig extends GuiceServletContextListener {
        @Override
        protected Injector getInjector() {
            return Guice.createInjector(new JerseyServletModule() {
                @Override
                protected void configureServlets() {
                    analyses = mock(Analyses.class);
                    bind(ApplicationsManager.class).to(MockApplicationsManager.class).asEagerSingleton();
                    bind(Analyses.class).toInstance(analyses);

                    // add REST services
                    bind(ApplicationService.class);
                    bind(AnalysisService.class);

                    // add bindings for Jackson
                    bind(JacksonJaxbJsonProvider.class).asEagerSingleton();
                    bind(JacksonMixInProvider.class).asEagerSingleton();
                    bind(MessageBodyReader.class).to(JacksonJsonProvider.class);
                    bind(MessageBodyWriter.class).to(JacksonJsonProvider.class);

                    // Route all requests through GuiceContainer
                    serve("/*").with(GuiceContainer.class);
                    filter("/*").through(GuiceContainer.class);
                }
            });
        }
    }
}