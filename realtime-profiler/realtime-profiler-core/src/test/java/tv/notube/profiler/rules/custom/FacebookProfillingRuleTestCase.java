package tv.notube.profiler.rules.custom;

import org.testng.Assert;
import org.testng.annotations.Test;
import tv.notube.commons.cogito.CogitoNLPEngineImpl;
import tv.notube.commons.lupedia.LUpediaNLPEngineImpl;
import tv.notube.commons.model.activity.Object;
import tv.notube.profiler.rules.ObjectProfilingRule;
import tv.notube.profiler.rules.ProfilingRuleException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class FacebookProfillingRuleTestCase {

    private static final String endpoint = "http://test.expertsystem.it/IPTC_ITA/EssexWS.asmx/ESSEXIndexdata";

    private ObjectProfilingRule<tv.notube.commons.model.activity.Object> rule;

    @Test
    public void testSimpleLike() throws ProfilingRuleException, URISyntaxException, MalformedURLException {
        rule = new GenericObjectProfilingRule(
                getSimpleLike(),
                new LUpediaNLPEngineImpl(),
                null
        );
        rule.run(null);
        Collection<URI> actual = rule.getResult();
        Assert.assertEquals(actual.size(), 0);
    }

    @Test
    public void testSimpleShare() throws ProfilingRuleException, URISyntaxException, MalformedURLException {
        rule = new GenericObjectProfilingRule(
                getSimpleFeed(),
                new CogitoNLPEngineImpl(endpoint),
                null
        );
        rule.run(null);
        Collection<URI> actual = rule.getResult();
        Assert.assertEquals(actual.size(), 4);
        Assert.assertTrue(actual.contains(new URI("http://dati.rai.tv/category/software")));
        Assert.assertTrue(actual.contains(new URI("http://dati.rai.tv/category/informatica")));
        Assert.assertTrue(actual.contains(new URI("http://dati.rai.tv/category/internet")));
    }

    @Test
    public void testAnotherSimpleShare() throws ProfilingRuleException, URISyntaxException, MalformedURLException {
        rule = new GenericObjectProfilingRule(
                getAnotherSimpleFeed(),
                new CogitoNLPEngineImpl(endpoint),
                null
        );
        rule.run(null);
        Collection<URI> actual = rule.getResult();
        Assert.assertEquals(actual.size(), 0);
    }

    @Test
    public void testComplexShare() throws ProfilingRuleException, URISyntaxException, MalformedURLException {
        rule = new GenericObjectProfilingRule(
                getComplexFeed(),
                new CogitoNLPEngineImpl(endpoint),
                null
        );
        rule.run(null);
        Collection<URI> actual = rule.getResult();
        Assert.assertEquals(actual.size(), 8);
        Assert.assertTrue(actual.contains(new URI("http://dati.rai.tv/category/sport")));
        Assert.assertTrue(actual.contains(new URI("http://dati.rai.tv/category/cinema")));
        Assert.assertTrue(actual.contains(new URI("http://dati.rai.tv/category/internet")));
    }

    private tv.notube.commons.model.activity.Object getSimpleLike() throws MalformedURLException {
        tv.notube.commons.model.activity.Object o = new Object();
        o.setDescription("Food/beverages");
        o.setUrl(new URL("http://www.facebook.com/39332170515"));
        o.setName("Carbonara");
        return o;
    }

    private tv.notube.commons.model.activity.Object getSimpleFeed() throws MalformedURLException {
        tv.notube.commons.model.activity.Object o = new Object();
        o.setDescription("A website dedicated to the fascinating world of mathematics and programming");
        o.setUrl(new URL("http://projecteuler.net/"));
        o.setName("Project Euler");
        return o;
    }

    private tv.notube.commons.model.activity.Object getAnotherSimpleFeed() throws MalformedURLException {
        tv.notube.commons.model.activity.Object o = new Object();
        o.setDescription("A website dedicated to the fascinating world of mathematics and programming");
        o.setUrl(new URL("http://9gag.com/gag/4431839"));
        o.setName("Barney being awesome!");
        return o;
    }

    private tv.notube.commons.model.activity.Object getComplexFeed() throws MalformedURLException {
        tv.notube.commons.model.activity.Object o = new Object();
        o.setDescription("Click to see the pic and write a comment...");
        o.setUrl(new URL("http://www.bbc.co.uk/news/uk-18494541"));
        o.setName("Doctor Who star Matt Smith jokes at lack of 2012 ticket");
        return o;
    }

}