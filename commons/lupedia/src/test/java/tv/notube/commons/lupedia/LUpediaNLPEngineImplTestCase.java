package tv.notube.commons.lupedia;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.nlp.NLPEngine;
import tv.notube.commons.nlp.NLPEngineException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class LUpediaNLPEngineImplTestCase {

    private NLPEngine nlpEngine;

    @BeforeTest
    public void setUp() {
        nlpEngine = new LUpediaNLPEngineImpl();
    }

    @Test
    public void test() throws NLPEngineException, URISyntaxException {
        final String text = "London is a great city, but Rome is amazing too.";
        Collection<URI> uris = nlpEngine.enrich(text);
        Assert.assertEquals(uris.size(), 2);
        Assert.assertTrue(uris.contains(new URI("http://dbpedia.org/resource/London")));
        Assert.assertTrue(uris.contains(new URI("http://dbpedia.org/resource/Rome")));
    }

}
