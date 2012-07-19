package tv.notube.commons.cogito;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.nlp.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

/**
 * Reference test case for {@link CogitoNLPEngineImpl}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class CogitoNLPEngineImplTest {

    private NLPEngine nlp;

    private static final String ENDPOINT = "http://test.expertsystem.it/IPTC_ITA/EssexWS.asmx/ESSEXIndexdata";

    @BeforeTest
    public void setUp() {
        nlp = new CogitoNLPEngineImpl(ENDPOINT);
    }

    @Test
    public void testTweet() throws NLPEngineException, URISyntaxException {
        NLPEngineResult result = nlp.enrich("London is a great city, but Rome is amazing too.");
        Assert.assertNotNull(result);
        Set<Entity> entities = result.getEntities();
        Assert.assertEquals(entities.size(), 2);
        Set<URI> actual = toURISet(entities);
        Assert.assertTrue(actual.contains(new URI("http://dati.rai.tv/entity/14200955")));
        Assert.assertTrue(actual.contains(new URI("http://dati.rai.tv/entity/12631236")));
    }

    @Test
    public void testBigText() throws NLPEngineException, MalformedURLException, URISyntaxException {
        String text = getText("/big-text.txt");
        NLPEngineResult result = nlp.enrich(text);
        Assert.assertNotNull(result);
        Set<Entity> entities = result.getEntities();
        Assert.assertEquals(entities.size(), 11);
        Set<URI> actual = toURISet(entities);
        Assert.assertTrue(actual.contains(new URI("http://dati.rai.tv/entity/12511119")));
        Assert.assertTrue(actual.contains(new URI("http://dati.rai.tv/entity/2948")));
        Assert.assertTrue(actual.contains(new URI("http://dati.rai.tv/entity/73032")));

        Set<Category> categories = result.getCategories();
        Assert.assertEquals(categories.size(), 5);
        actual = toURISet(categories);
        Assert.assertTrue(actual.contains(new URI("http://dati.rai.tv/category/istituzioni")));
        Assert.assertTrue(actual.contains(new URI("http://dati.rai.tv/category/politica")));
        Assert.assertTrue(actual.contains(new URI("http://dati.rai.tv/category/ministeri")));
        Assert.assertTrue(actual.contains(new URI("http://dati.rai.tv/category/parlamento")));
        Assert.assertTrue(actual.contains(new URI("http://dati.rai.tv/category/diritto+pubblico+e+amministrativo")));
    }

    private String getText(String fileName) {
        InputStream is = CogitoNLPEngineImplTest.class.getResourceAsStream(fileName);
        try {
            return IOUtils.toString(is);
        } catch (IOException e) {
            throw new RuntimeException("Error while reading [" + fileName + "]", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new RuntimeException("Error while closing stream", e);
            }
        }
    }

    private Set<URI> toURISet(Set<? extends Feature> entities) {
        Set<URI> uris = new HashSet<URI>();
        for(Feature entity : entities) {
            uris.add(entity.getResource());
        }

        return uris;
    }
}