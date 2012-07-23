package tv.notube.commons.linking;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Reference test case for {@link FacebookCogitoLinkingEngine}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FacebookCogitoLinkingEngineTestCase {

    private LinkingEngine linkingEngine;

    @BeforeTest
    public void setUp() {
        linkingEngine = new FacebookCogitoLinkingEngine();
    }

    @Test
    public void testShouldFindALink() throws LinkingEngineException {
        final String ogCat1 = "politics";
        String actual = linkingEngine.link(ogCat1);
        Assert.assertNotNull(actual);
        Assert.assertEquals(actual, "politica");
    }

    @Test
    public void testShouldNotFindALink() throws LinkingEngineException {
        final String ogCat1 = "canale radiofonico";
        try {
            linkingEngine.link(ogCat1);
        } catch (LinkNotFoundException e) {
            Assert.assertTrue(true);
            return;
        }
        Assert.assertTrue(false);
    }

    @Test
    public void testWithSpacesInSource() throws LinkingEngineException {
        final String ogCat1 = "radio station";
        String actual = linkingEngine.link(ogCat1);
        Assert.assertNotNull(actual);
        Assert.assertEquals(actual, "canale radiofonico");
    }

}
