package tv.notube.commons.model.activity.bbc;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class GenreBuilderTestCase {

    private GenreBuilder builder;

    @BeforeTest
    public void setUp() {
        builder = GenreBuilder.getInstance();
    }

    @AfterTest
    public void tearDown() {
        builder = null;
    }

    @Test(enabled = false)
    public void test() throws MalformedURLException {
        BBCGenre bbcGenre = builder.lookup(
                new URL("http://www.bbc.co.uk/programmes/genres/sport#genre")
        );
        Assert.assertNotNull(bbcGenre);
    }

}
