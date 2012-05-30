package tv.notube.extension.profilingline.lupedia;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Reference test for {@link DefaultLupediaImpl} class.
 *
 * @author Davide Palmisano (dpalmisano@gmail.com)
 */
public class DefaultLupediaImplTestCase {

    private Lupedia lupedia;

    @BeforeTest
    public void setUp() {
        lupedia = new DefaultLupediaImpl();
    }

    @AfterTest
    public void tearDown() {
        lupedia = null;
    }

    @Test(enabled = false)
    public void testGetResources() throws LupediaException, URISyntaxException {
        final String text = "Bassiano is a small city located 100 kms south of Rome";
        List<URI> uris = lupedia.getResources(text);
        Assert.assertNotNull(uris);
        Assert.assertEquals(uris.size(), 2);
        Assert.assertEquals(uris.get(0), new URI("http://dbpedia.org/resource/Bassiano"));
        Assert.assertEquals(uris.get(1), new URI("http://dbpedia.org/resource/Rome"));
    }

}
