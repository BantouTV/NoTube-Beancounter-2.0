package tv.notube.listener.facebook.converter;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.model.activity.facebook.Like;
import tv.notube.listener.facebook.model.FacebookData;

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
    public void test() throws ConverterException {
        Like like = flc.convert(getFacebookData(), true);
        Assert.assertNotNull(like);
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
