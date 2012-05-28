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
    public void stringRandomiser() {
        StringRandomiser sr = new StringRandomiser("string-randomizer", 2, 15);
        // exec it several time
        for (int i = 0; i < 100; i++) {
            String randomString = sr.getRandom();
            Assert.assertNotNull(randomString);
            Assert.assertTrue(randomString.length() > 0);
            System.out.println(randomString);
        }
        sr = new StringRandomiser("string-randomizer", 2, 15, false);
        // exec it several time
        for (int i = 0; i < 100; i++) {
            String randomString = sr.getRandom();
            Assert.assertNotNull(randomString);
            Assert.assertTrue(randomString.length() > 0);
            System.out.println(randomString);
        }
    }

}
