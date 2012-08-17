package io.beancounter.commons.helper;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Properties;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class PropertiesHelperTestCase {

    @Test
    public void testReadFromFileSystem() {
        String userDir = System.getProperty("user.dir");
        Properties properties = PropertiesHelper.readFromFileSystem(userDir + "/src/test/resources/test.properties");
        Assert.assertNotNull(properties);
        Assert.assertEquals(
                properties.getProperty("field1.subfield1.key"),
                "value11"
        );
        Assert.assertEquals(
                properties.getProperty("field2.key"),
                "value2"
        );
        Assert.assertEquals(
                properties.getProperty("field2.subfield1.key"),
                "value21"
        );
    }

    @Test
    public void testFromClasspath() {
        Properties properties = PropertiesHelper.readFromClasspath("/test.properties");
        Assert.assertNotNull(properties);
        Assert.assertEquals(
                properties.getProperty("field1.subfield1.key"),
                "value11"
        );
        Assert.assertEquals(
                properties.getProperty("field2.key"),
                "value2"
        );
        Assert.assertEquals(
                properties.getProperty("field2.subfield1.key"),
                "value21"
        );

    }

}
