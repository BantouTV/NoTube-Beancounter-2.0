package tv.notube.commons.cogito;

import org.testng.Assert;
import org.testng.annotations.Test;
import tv.notube.commons.nlp.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class CogitoNLPEngineImplTest {

    @Test
    public void testTweet() throws NLPEngineException, URISyntaxException {
        CogitoNLPEngineImpl nlp = new CogitoNLPEngineImpl(null);
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
        // TODO (med) make it static
        String url = "http://ansa.it/web/notizie/rubriche/associata/2012/07/18/CRISI-NAPOLITANO-OGGI-INCONTRO-URGENTE-MONTI_7201843.html";
        CogitoNLPEngineImpl nlp = new CogitoNLPEngineImpl(null);
        NLPEngineResult result = nlp.enrich(new URL(url));
        Assert.assertNotNull(result);
        Set<Entity> entities = result.getEntities();
        Assert.assertEquals(entities.size(), 3);
        Set<URI> actual = toURISet(entities);
        Assert.assertTrue(actual.contains(new URI("http://dati.rai.tv/entity/100874")));
        Assert.assertTrue(actual.contains(new URI("http://dati.rai.tv/entity/13154278")));
        Assert.assertTrue(actual.contains(new URI("http://dati.rai.tv/entity/255959")));

        Set<Category> categories = result.getCategories();
        Assert.assertEquals(categories.size(), 3);
        actual = toURISet(categories);
        Assert.assertTrue(actual.contains(new URI("http://dati.rai.tv/category/istituzioni")));
        Assert.assertTrue(actual.contains(new URI("http://dati.rai.tv/category/politica")));
        Assert.assertTrue(actual.contains(new URI("http://dati.rai.tv/category/diritto+pubblico+e+amministrativo")));
    }

    private Set<URI> toURISet(Set<? extends Feature> entities) {
        Set<URI> uris = new HashSet<URI>();
        for(Feature entity : entities) {
            uris.add(entity.getResource());
        }

        return uris;
    }
}