package io.beancounter.platform;

import com.google.inject.servlet.GuiceFilter;
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import io.beancounter.platform.responses.ApplicationPlatformResponse;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public abstract class AbstractJerseyTestCase {

    protected static final Logger logger = Logger.getLogger(AbstractJerseyTestCase.class);

    private static final String base_uri_str = "http://localhost:%d/rest/";

    protected final URI base_uri;

    protected GrizzlyWebServer server;

    protected AbstractJerseyTestCase(int port) {
        try {
            base_uri = new URI(String.format(base_uri_str, port));
        } catch (URISyntaxException urise) {
            throw new RuntimeException(urise);
        }
    }

    @BeforeTest
    public void setUp() throws Exception {
        startFrontendService();
    }

    protected void startFrontendService() throws IOException {
        server = new GrizzlyWebServer(9995);
        ServletAdapter ga = new ServletAdapter();
        ga.addServletListener(TestServiceConfig.class.getName());
        ga.setServletPath("/");
        ga.addFilter(new GuiceFilter(), "filter", null);
        server.addGrizzlyAdapter(ga, null);
        server.start();
    }

    @AfterTest
    public void tearDown() throws InterruptedException {
        server.stop();
    }

    protected <T> T fromJson(String responseBody, Class<T> t) throws IOException {
        return (new ObjectMapper()).readValue(responseBody, t);
    }

    protected UUID registerTestApplication() throws IOException {
        String baseQuery = "application/register";
        final String name = "fake_application_name";
        final String description = "This is a test registration!";
        final String email = "fake_mail@test.com";
        final String oauth = "http://fakeUrlOAUTH";
        PostMethod postMethod = new PostMethod(base_uri + baseQuery);
        HttpClient client = new HttpClient();
        postMethod.addParameter("name", name);
        postMethod.addParameter("description", description);
        postMethod.addParameter("email", email);
        postMethod.addParameter("oauthCallback", oauth);
        client.executeMethod(postMethod);
        String responseBody = new String(postMethod.getResponseBody());
        ApplicationPlatformResponse actual = fromJson(responseBody, ApplicationPlatformResponse.class);
        return actual.getObject().getAdminKey();
    }

}
