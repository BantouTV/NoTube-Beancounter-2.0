package tv.notube.commons.regexapi;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Main reference test class for {@link RegexAPI}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class RegexAPITestCase {

    private RegexAPI RegexAPI;

    @BeforeTest
    public void setUp() {
        RegexAPI = new RegexAPI("04490000a72fe7ec5cb3497f14e77f338c86f2fe");
    }

    @AfterTest
    public void tearDown() {
        RegexAPI = null;
    }

    @Test(enabled = false)
    public void testGetRankedConcept() throws RegexAPIException {
        final String text = "a sentence about Franz Ferdinand, which is " +
                "the name of a band and some old dead guy. Sausages. iPhone. RDF";
        RegexAPIResponse RegexAPIResponse = RegexAPI.getRankedConcept(
                text
        );
        Assert.assertNotNull(RegexAPIResponse);
        Assert.assertEquals(RegexAPIResponse.getIdentified().size(), 1);
    }

    @Test(enabled = false)
    public void testGetNamedEntities() throws RegexAPIException {
        final String text = "Samuel Palmisano is the CEO of IBM inc. and he lives in New York";
        RegexAPIResponse RegexAPIResponse = RegexAPI.getNamedEntities(
                text
        );
        Assert.assertNotNull(RegexAPIResponse);
        Assert.assertEquals(RegexAPIResponse.getIdentified().size(), 1);
    }

    @Test(enabled = false)
    public void testGetWebNamedEntities() throws RegexAPIException, MalformedURLException {
        final URL url = new URL("http://www.guardian.co.uk/theguardian/2011/nov/19/read-serious-books-zoe-williams?INTCMP=SRCH");
        RegexAPIResponse RegexAPIResponse = RegexAPI.getNamedEntities(
                url
        );
        Assert.assertNotNull(RegexAPIResponse);
        Assert.assertTrue(RegexAPIResponse.getIdentified().size() > 0);
    }

    @Test(enabled = false)
    public void testGetWebRankedConcepts() throws RegexAPIException,
            MalformedURLException {
        final URL url = new URL("http://www.guardian.co.uk/theguardian/2011/nov/19/read-serious-books-zoe-williams?INTCMP=SRCH");
        RegexAPIResponse RegexAPIResponse = RegexAPI.getRankedConcept(
                url
        );
        Assert.assertNotNull(RegexAPIResponse);
        Assert.assertTrue(RegexAPIResponse.getIdentified().size() > 0);
    }


}
