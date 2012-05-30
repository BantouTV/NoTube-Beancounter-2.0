package tv.notube.commons.alchemyapi;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Main reference class for {@link AlchemyAPI}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class AlchemyAPITestCase {

    private AlchemyAPI alchemyAPI;

    @BeforeTest
    public void setUp() {
        alchemyAPI = new AlchemyAPI("04490000a72fe7ec5cb3497f14e77f338c86f2fe");
    }

    @AfterTest
    public void tearDown() {
        alchemyAPI = null;
    }

    @Test(enabled = false)
    public void testGetRankedConcept() throws AlchemyAPIException {
        final String text = "Rome is the capital of Italy, " +
                "a european southern country.";
        AlchemyAPIResponse alchemyAPIResponse = alchemyAPI.getRankedConcept(
                text
        );
        Assert.assertNotNull(alchemyAPIResponse);
        Assert.assertEquals(alchemyAPIResponse.getIdentified().size(), 8);
    }

    @Test(enabled = false)
    public void testGetNamedEntities() throws AlchemyAPIException {
        final String text = "Samuel Palmisano is the CEO of IBM inc. and he " +
                "lives in New York";
        AlchemyAPIResponse alchemyAPIResponse = alchemyAPI.getNamedEntities(
                text
        );
        Assert.assertNotNull(alchemyAPIResponse);
        Assert.assertEquals(alchemyAPIResponse.getIdentified().size(), 1);
    }

    @Test(enabled = false)
    public void testGetWebNamedEntities() throws AlchemyAPIException, MalformedURLException {
        final URL url = new URL("http://www.guardian.co.uk/theguardian/2011/nov/19/read-serious-books-zoe-williams?INTCMP=SRCH");
        AlchemyAPIResponse alchemyAPIResponse = alchemyAPI.getNamedEntities(
                url
        );
        Assert.assertNotNull(alchemyAPIResponse);
        Assert.assertTrue(alchemyAPIResponse.getIdentified().size() > 0);
    }

    @Test(enabled = false)
    public void testGetWebRankedConcepts() throws AlchemyAPIException,
            MalformedURLException {
        final URL url = new URL("http://www.guardian.co.uk/theguardian/2011/nov/19/read-serious-books-zoe-williams?INTCMP=SRCH");
        AlchemyAPIResponse alchemyAPIResponse = alchemyAPI.getRankedConcept(
                url
        );
        Assert.assertNotNull(alchemyAPIResponse);
        Assert.assertEquals(alchemyAPIResponse.getIdentified().size(), 1);
    }

}
