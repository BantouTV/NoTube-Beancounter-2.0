package tv.notube.commons.storage.model.fields.serialization;

import org.testng.Assert;
import org.testng.annotations.Test;
import tv.notube.commons.storage.model.fields.Bytes;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class SerializationManagerTestCase {

    @Test(enabled = false)
    public void testSerialize() throws MalformedURLException, SerializationManagerException {
        SerializationManager sm = new SerializationManager();
        TestClass expected = new TestClass();
        expected.setAge(45);
        expected.setName("Jonh Malkovich");
        expected.setWebsite(new URL("http://google.com/index.html"));
        Bytes bytes = sm.serialize(expected);
        Assert.assertNotNull(bytes);

        TestClass actual = (TestClass) sm.deserialize(bytes.getBytes());
        Assert.assertNotNull(actual);

        Assert.assertEquals(expected, actual);
    }

}
