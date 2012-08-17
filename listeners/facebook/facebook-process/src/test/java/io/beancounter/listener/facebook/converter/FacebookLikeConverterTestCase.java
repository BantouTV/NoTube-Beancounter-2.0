package io.beancounter.listener.facebook.converter;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import io.beancounter.commons.model.activity.facebook.Like;
import io.beancounter.listener.facebook.core.converter.FacebookActivityConverterException;
import io.beancounter.listener.facebook.core.converter.custom.ConverterException;
import io.beancounter.listener.facebook.core.converter.custom.FacebookLikeConverter;
import io.beancounter.listener.facebook.core.model.FacebookData;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FacebookLikeConverterTestCase {

    private FacebookLikeConverter flc;

    @BeforeTest
    public void setUp() throws FacebookActivityConverterException {
        flc = new FacebookLikeConverter();
    }

    @Test
    public void test() throws ConverterException, MalformedURLException {
        Like like = flc.convert(getFacebookData(), true);
        Assert.assertNotNull(like);
        Assert.assertTrue(like.getCategories().iterator().next().equals("Radio station"));
        Assert.assertTrue(like.getName().equals("Radio Globo"));
        Assert.assertTrue(like.getUrl().equals(new URL("http://www.facebook.com/53420726443")));
    }

    private FacebookData getFacebookData() {
        FacebookDataTest fbd = new FacebookDataTest();
        fbd.setName("Radio Globo");
        fbd.setCategory("Radio station");
        fbd.setCreatedTime("2011-12-23T13:14:41+0000");
        fbd.setId("53420726443");
        return fbd;
    }

}
