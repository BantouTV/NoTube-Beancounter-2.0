package tv.notube.commons.lupedia;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.nlp.NLPEngine;
import tv.notube.commons.nlp.NLPEngineException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
    public void testText() throws NLPEngineException, URISyntaxException {
        final String text = "London is a great city, but Rome is amazing too.";
        Collection<URI> uris = nlpEngine.enrich(text);
        Assert.assertEquals(uris.size(), 2);
        Assert.assertTrue(uris.contains(new URI("http://dbpedia.org/resource/London")));
        Assert.assertTrue(uris.contains(new URI("http://dbpedia.org/resource/Rome")));
    }

    @Test
    public void testUrl() throws NLPEngineException, URISyntaxException, MalformedURLException {
        final URL url = new URL("http://www.bbc.co.uk/news/uk-18494541");
        Collection<URI> uris = nlpEngine.enrich(url);
        Assert.assertEquals(uris.size(), 5);
        Assert.assertTrue(uris.contains(new URI("http://dbpedia.org/resource/London")));
        Assert.assertTrue(uris.contains(new URI("http://dbpedia.org/resource/BBC")));
        Assert.assertTrue(uris.contains(new URI("http://dbpedia.org/resource/Cardiff")));
        Assert.assertTrue(uris.contains(new URI("http://dbpedia.org/resource/Barnsley")));
    }

}
