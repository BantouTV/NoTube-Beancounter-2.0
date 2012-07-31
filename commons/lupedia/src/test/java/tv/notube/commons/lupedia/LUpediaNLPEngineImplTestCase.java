package tv.notube.commons.lupedia;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.nlp.Entity;
import tv.notube.commons.nlp.NLPEngine;
import tv.notube.commons.nlp.NLPEngineException;
import tv.notube.commons.nlp.NLPEngineResult;
import tv.notube.commons.redirects.RedirectException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

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
    public void testSimpleText() throws NLPEngineException, URISyntaxException {
        final String text = "London is a great city, but Rome is amazing too.";
        NLPEngineResult result = nlpEngine.enrich(text);
        Set<Entity> entities = result.getEntities();
        Assert.assertEquals(entities.size(), 2);
        Set<URI> actual = toURISet(entities);
        Assert.assertTrue(actual.contains(new URI("http://dbpedia.org/resource/London")));
        Assert.assertTrue(actual.contains(new URI("http://dbpedia.org/resource/Rome")));
    }

    @Test
    public void testTweetText() throws NLPEngineException, URISyntaxException {
        final String text = "@Hare_F1 @MagnificentGeof and lets face it, it's more likely to happen than a London GP!";
        NLPEngineResult result = nlpEngine.enrich(text);
        Set<Entity> entities = result.getEntities();
        Assert.assertEquals(entities.size(), 1);
        Set<URI> actual = toURISet(entities);
        Assert.assertTrue(actual.contains(new URI("http://dbpedia.org/resource/London")));
    }

    @Test
    public void testUrl() throws NLPEngineException, URISyntaxException, MalformedURLException {
        final URL url = new URL("http://www.bbc.co.uk/news/uk-18494541");
        NLPEngineResult result = nlpEngine.enrich(url);
        Set<Entity> entities = result.getEntities();
        Assert.assertEquals(entities.size(), 7);
        Set<URI> actual = toURISet(entities);
        Assert.assertTrue(actual.contains(new URI("http://dbpedia.org/resource/BBC")));
        Assert.assertTrue(actual.contains(new URI("http://dbpedia.org/resource/Barnsley")));
        Assert.assertTrue(actual.contains(new URI("http://dbpedia.org/resource/London")));
        Assert.assertTrue(actual.contains(new URI("http://dbpedia.org/resource/Cardiff")));
    }

    @Test
    public void testUrlBigText() throws NLPEngineException, URISyntaxException, MalformedURLException {
        final URL url = new URL("http://www.teamgb.com/news/eighteen-gymnasts-selected-team-gb-london-2012");
        NLPEngineResult result = nlpEngine.enrich(url);
        Set<Entity> entities = result.getEntities();
        Assert.assertEquals(entities.size(), 25);
        Set<URI> actual = toURISet(entities);
        Assert.assertTrue(actual.contains(new URI("http://dbpedia.org/resource/London")));
        Assert.assertTrue(actual.contains(new URI("http://dbpedia.org/resource/Athens")));
        Assert.assertTrue(actual.contains(new URI("http://dbpedia.org/resource/Beijing")));
        Assert.assertTrue(actual.contains(new URI("http://dbpedia.org/resource/Glasgow")));
        Assert.assertTrue(actual.contains(new URI("http://dbpedia.org/resource/World")));
    }

    @Test
    public void testTinyUrl() throws NLPEngineException, URISyntaxException, MalformedURLException {
        final URL url = new URL("http://bit.ly/4XzVxm");
        NLPEngineResult result = nlpEngine.enrich(url);
        Set<Entity> entities = result.getEntities();
        Assert.assertEquals(entities.size(), 15);
        Set<URI> actual = toURISet(entities);
        Assert.assertTrue(actual.contains(new URI("http://dbpedia.org/resource/Google")));
    }

    @Test
    public void testTinyUrlTwo() throws NLPEngineException, URISyntaxException, MalformedURLException {
        final URL url = new URL("http://ow.ly/1kDDFe");
        NLPEngineResult result;
        try {
            result = nlpEngine.enrich(url);
        } catch (NLPEngineException e) {
            Assert.assertTrue(e.getCause() instanceof RedirectException);
            Assert.assertEquals(
                    e.getCause().getMessage(),
                    "Error while creating the new url for the redirect [/hotel/superPage.jsp?startDate=06%2F22%2F2012&encDealHash=MTAwOjE3MzAyNjo4OTcxOTo0LjA6NDkuOTk5OTk2Olk6WTpZ&rid=r-69820702248&wid=w-3&xid=x-103&rs=20500&endDate=06%2F24%2F2012]"
            );
        }
    }

    @Test
    public void testNotFoundUrl() throws NLPEngineException, URISyntaxException, MalformedURLException {
        final URL url = new URL("http://www.cedarsummit.com/error");
        NLPEngineResult result;
        try {
            result = nlpEngine.enrich(url);
        } catch (NLPEngineException e) {
            Assert.assertTrue(e.getCause() instanceof RedirectException);
            Assert.assertEquals(
                    e.getCause().getMessage(),
                    "Seem that the redirect for [http://www.cedarsummit.com/error] went wrong [error code: 404]"
            );
        }
    }

    @Test(enabled = false)
    public void testDifferentErrorUrl() throws NLPEngineException, URISyntaxException, MalformedURLException {
        final URL url = new URL("http://api.beancounter.io/rest/user/ndkcee?apikey=14fb6663");
        NLPEngineResult result;
        try {
            result = nlpEngine.enrich(url);
        } catch (NLPEngineException e) {
            Assert.assertTrue(e.getCause() instanceof RedirectException);
            Assert.assertEquals(
                    e.getCause().getMessage(),
                    "Seem that the redirect for [http://api.beancounter.io/rest/user/ndkcee?apikey=14fb6663]" +
                            " went wrong [error code: 500]"
            );
        }
    }

    private Set<URI> toURISet(Set<Entity> entities) {
        Set<URI> uris = new HashSet<URI>();
        for (Entity entity : entities) {
            uris.add(entity.getResource());
        }
        return uris;
    }

}
