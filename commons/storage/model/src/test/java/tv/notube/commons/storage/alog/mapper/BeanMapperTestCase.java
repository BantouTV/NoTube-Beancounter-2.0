package tv.notube.commons.storage.alog.mapper;

import junit.framework.Assert;
import org.joda.time.DateTime;
import org.testng.annotations.Test;
import tv.notube.commons.storage.model.fields.Field;
import tv.notube.commons.storage.model.mapper.BeanMapper;
import tv.notube.commons.storage.model.mapper.BeanMapperException;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Reference test case for {@link tv.notube.commons.storage.model.mapper.BeanMapper}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class BeanMapperTestCase {

    @Test(enabled = false)
    public void testFieldsMapping() throws MalformedURLException, BeanMapperException {
        BeanMapper<TestObject> bm = new BeanMapper<TestObject>();
        TestObject expected = new TestObject();
        expected.setDatetime(new DateTime());
        expected.setInteger(564);
        expected.setString("just a string");
        expected.setUrl(new URL("http://google.com"));
        Field fields[] = bm.toFields(expected);

        Assert.assertEquals(4, fields.length);

        TestObject actual = bm.toObject(fields, TestObject.class);
        Assert.assertNotNull(actual);
        Assert.assertEquals(expected, actual);
    }

}
