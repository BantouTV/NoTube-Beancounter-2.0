package tv.notube.commons.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class RandomiserTestCase {

    @Test
    public void test() {
        StringRandomiser sr = new StringRandomiser("string-randomizer");
        String randomString = sr.getRandom();
        Assert.assertNotNull(randomString);
        Assert.assertTrue(randomString.length() > 0);
    }

}
