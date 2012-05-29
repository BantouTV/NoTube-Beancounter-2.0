package tv.noube.crawler;

import com.google.inject.servlet.GuiceFilter;
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class AbstractCrawlerTestCase {

    protected static final Logger logger = Logger.getLogger(AbstractCrawlerTestCase.class);

    private static final String base_uri_str = "http://localhost:%d/";

    protected final URI base_uri;

    private GrizzlyWebServer server;

    protected AbstractCrawlerTestCase(int port) {
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
}
