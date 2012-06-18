package tv.notube.commons.lupedia;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.nlp.NLPEngine;
import tv.notube.commons.nlp.NLPEngineException;

import java.net.URI;
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
    public void test() throws NLPEngineException {
        final String text = "hey darling, do you know that London will host the 2012 Olympic Games?";
        Collection<URI> uris = nlpEngine.enrich(text);
        Assert.assertEquals(uris.size(), 1);
    }

}
