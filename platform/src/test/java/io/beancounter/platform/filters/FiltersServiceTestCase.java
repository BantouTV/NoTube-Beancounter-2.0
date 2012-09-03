package io.beancounter.platform.filters;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import io.beancounter.applications.ApplicationsManager;
import io.beancounter.applications.MockApplicationsManager;
import io.beancounter.filter.manager.FilterManager;
import io.beancounter.filter.model.Filter;
import io.beancounter.filter.model.pattern.ActivityPattern;
import io.beancounter.platform.APIResponse;
import io.beancounter.platform.AbstractJerseyTestCase;
import io.beancounter.platform.ApplicationService;
import io.beancounter.platform.FilterService;
import io.beancounter.platform.JacksonMixInProvider;
import io.beancounter.platform.responses.FilterPlatformResponse;
import io.beancounter.platform.responses.StringPlatformResponse;
import io.beancounter.platform.responses.StringsPlatformResponse;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Reference test case for {@link io.beancounter.platform.FilterService}
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FiltersServiceTestCase extends AbstractJerseyTestCase {

    private static String APIKEY;

    private static FilterManager filterManager;

    protected FiltersServiceTestCase() {
        super(9995);
    }

    @Override
    protected void startFrontendService() throws IOException {
        server = new GrizzlyWebServer(9995);
        ServletAdapter ga = new ServletAdapter();
        ga.addServletListener(FilterServiceTestConfig.class.getName());
        ga.setServletPath("/");
        ga.addFilter(new GuiceFilter(), "filter", null);
        server.addGrizzlyAdapter(ga, null);
        server.start();
    }

    private UUID registerTestApplication() throws IOException {
        String baseQuery = "application/register";
        String name = "fake_application_name";
        String description = "This is a test registration!";
        String email = "fake_mail@test.com";
        String oauth = "http://fakeUrlOAUTH";

        PostMethod postMethod = new PostMethod(base_uri + baseQuery);
        HttpClient client = new HttpClient();
        postMethod.addParameter("name", name);
        postMethod.addParameter("description", description);
        postMethod.addParameter("email", email);
        postMethod.addParameter("oauthCallback", oauth);
        client.executeMethod(postMethod);

        String responseBody = new String(postMethod.getResponseBody());
        APIResponse actual = fromJson(responseBody, APIResponse.class);
        return UUID.fromString(actual.getObject());
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
        reset(filterManager);
    }

    @Test
    public void registeringFilterWithNoQueuesShouldBeRespondWithError() throws Exception {
        String baseQuery = "filters/register/%s?apikey=%s";
        String name = "social-event-filter";
        String description = "this filter filters all the activities matching a given event";
        String pattern = patternJson();
        String query = String.format(
                baseQuery,
                name,
                APIKEY
        );

        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("pattern", pattern);
        postMethod.addParameter("description", description);

        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.NOK);
        assertEquals(response.getMessage(), "You must specify at least one queue");
    }

    @Test
    public void registeringFilterWithOneQueueShouldBeSuccessful() throws Exception {
        String baseQuery = "filters/register/%s?apikey=%s";
        String name = "social-event-filter";
        String description = "this filter filters all the activities matching a given event";
        String pattern = patternJson();
        ActivityPattern activityPattern = fromJson(pattern, ActivityPattern.class);
        Set<String> queues = new HashSet<String>();
        queues.add("queue1");
        String query = String.format(
                baseQuery,
                name,
                APIKEY
        );

        when(filterManager.register(name, description, queues, activityPattern)).thenReturn(name);

        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("pattern", pattern);
        postMethod.addParameter("description", description);
        postMethod.addParameter("queue", "queue1");

        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.OK);
        assertEquals(response.getMessage(), "filter [" + name + "] successfully registered");
        assertEquals(response.getObject(), name);
    }

    @Test
    public void registeringFilterWithTwoQueuesShouldBeSuccessful() throws Exception {
        String baseQuery = "filters/register/%s?apikey=%s";
        String name = "social-event-filter";
        String description = "this filter filters all the activities matching a given event";
        String pattern = patternJson();
        ActivityPattern activityPattern = fromJson(pattern, ActivityPattern.class);
        Set<String> queues = new HashSet<String>();
        queues.add("queue1");
        queues.add("queue2");
        String query = String.format(
                baseQuery,
                name,
                APIKEY
        );

        when(filterManager.register(name, description, queues, activityPattern)).thenReturn(name);

        PostMethod postMethod = new PostMethod(base_uri + query);
        HttpClient client = new HttpClient();
        postMethod.addParameter("pattern", pattern);
        postMethod.addParameter("description", description);
        for (String queue : queues) {
            postMethod.addParameter("queue", queue);
        }

        int result = client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        StringPlatformResponse response = fromJson(responseBody, StringPlatformResponse.class);
        assertEquals(response.getStatus(), StringPlatformResponse.Status.OK);
        assertEquals(response.getMessage(), "filter [" + name + "] successfully registered");
        assertEquals(response.getObject(), name);
    }


    @Test
    public void getFilter() throws Exception {
        String baseQuery = "filters/%s?apikey=%s";
        String name = "social-event-filter";
        String query = String.format(
                baseQuery,
                name,
                APIKEY
        );

        Filter filter = new Filter(name, "description", ActivityPattern.ANY, new HashSet<String>());
        when(filterManager.get(name)).thenReturn(filter);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        FilterPlatformResponse response = fromJson(responseBody, FilterPlatformResponse.class);
        assertEquals(response.getStatus(), FilterPlatformResponse.Status.OK);
        assertEquals(response.getMessage(), "filter with name [" + name + "] found");
        assertEquals(response.getObject(), filter);
    }

    @Test
    public void getFilterNames() throws Exception {
        String baseQuery = "filters/list/all?apikey=%s";
        String query = String.format(
                baseQuery,
                APIKEY
        );

        Collection<String> filters = Arrays.asList("queue1", "queue2", "queue3");
        when(filterManager.getRegisteredFilters()).thenReturn(filters);

        GetMethod getMethod = new GetMethod(base_uri + query);
        HttpClient client = new HttpClient();

        int result = client.executeMethod(getMethod);
        String responseBody = new String(getMethod.getResponseBody());
        assertEquals(result, HttpStatus.SC_OK);
        assertFalse(responseBody.isEmpty());

        StringsPlatformResponse response = fromJson(responseBody, StringsPlatformResponse.class);
        assertEquals(response.getStatus(), StringsPlatformResponse.Status.OK);
        assertEquals(response.getMessage(), "[" + filters.size() + "] registered filters found");
        assertEquals(response.getObject(), filters);
    }

    @Test
    public void testSerialization() throws IOException {
        String filterJson = "{\n" +
                "    \"name\": \"test-filter\",\n" +
                "    \"description\": \"test-description\",\n" +
                "    \"definedAt\": 1342546262929,\n" +
                "    \"active\": false,\n" +
                "    \"activityPattern\": " + patternJson() +
                "}";

        Filter f = fromJson(filterJson, Filter.class);
        assertNotNull(f);
    }

    private String patternJson() {
        return "{\n" +
                "    \"userId\": {\n" +
                "        \"uuid\": null\n" +
                "    },\n" +
                "    \"verb\": {\n" +
                "        \"verb\": \"ANY\"\n" +
                "    },\n" +
                "    \"object\": {\n" +
                "        \"type\": \"io.beancounter.filter.model.pattern.rai.TVEventPattern\",\n" +
                "        \"typePattern\": {\n" +
                "            \"string\": \"io.beancounter.filter.model.pattern.rai.TVEventPattern\"\n" +
                "        },\n" +
                "        \"url\": {\n" +
                "            \"url\": null\n" +
                "        },\n" +
                "        \"uuidPattern\": {\n" +
                "            \"uuid\": \"2590fd3d-97ea-49bb-b7ec-04da7553bb0a\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"context\": {\n" +
                "        \"date\": {\n" +
                "            \"date\": 1342530815683,\n" +
                "            \"bool\": \"BEFORE\"\n" +
                "        },\n" +
                "        \"service\": {\n" +
                "            \"string\": \"\"\n" +
                "        },\n" +
                "        \"mood\": {\n" +
                "            \"string\": \"\"\n" +
                "        },\n" +
                "        \"username\": {\n" +
                "            \"string\": \"\"\n" +
                "        }\n" +
                "    }\n" +
                "}";
    }

    public static class FilterServiceTestConfig extends GuiceServletContextListener {
        @Override
        protected Injector getInjector() {
            return Guice.createInjector(new JerseyServletModule() {
                @Override
                protected void configureServlets() {
                    filterManager = mock(FilterManager.class);
                    bind(ApplicationsManager.class).to(MockApplicationsManager.class).asEagerSingleton();
                    bind(FilterManager.class).toInstance(filterManager);

                    // add REST services
                    bind(ApplicationService.class);
                    bind(FilterService.class);

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