package tv.notube.commons.storage.kvs;

import org.testng.Assert;
import tv.notube.commons.storage.kvs.configuration.ConfigurationManager;
import tv.notube.commons.storage.kvs.mybatis.MyBatisKVStore;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.storage.model.Query;
import tv.notube.commons.storage.model.fields.StringField;
import tv.notube.commons.storage.model.fields.serialization.SerializationManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class MyBatisKVStoreTestCase {

    private final static String CONFIG_FILE =
            "tv/notube/commons/storage/kvs/configuration/kvs-configuration.xml";

    private final static String TABLE = "test";

    private KVStore kVStore;

    @BeforeTest
    public void setUp() {
        Properties properties = ConfigurationManager.getInstance(CONFIG_FILE)
                .getKVStoreConfiguration()
                .getProperties();
        kVStore = new MyBatisKVStore(properties, new SerializationManager());
    }

    @AfterTest
    public void tearDown() {
        kVStore = null;
    }

    @Test(enabled = false)
    public void testCRUD() throws MalformedURLException, KVStoreException {
        final String KEY = "key-1";
        TestClass expected = new TestClass();
        expected.setBool(false);
        expected.setString("a test string");
        expected.setUrl(new URL("http://google.com"));

        StringField f1 = new StringField("name", "test-object");
        kVStore.setValue(TABLE, KEY, expected, f1);

        TestClass actual = (TestClass) kVStore.getValue(TABLE, KEY);
        Assert.assertEquals(expected, actual);

        List<String> actualKeys = kVStore.search(TABLE);
        Assert.assertEquals(1, actualKeys.size());
        Assert.assertEquals(KEY, actualKeys.get(0));

        StringField fields[] = kVStore.getFields(TABLE, KEY);
        Assert.assertEquals(1, fields.length);

        Assert.assertEquals(f1, fields[0]);

        kVStore.deleteValue(TABLE, KEY);
        actual = (TestClass) kVStore.getValue(TABLE, KEY);
        Assert.assertNull(actual);

        fields = kVStore.getFields(TABLE, KEY);
        Assert.assertEquals(0, fields.length);
    }

    @Test(enabled = false)
    public void testWithQuery() throws MalformedURLException, KVStoreException {
        for (int i = 0; i < 100; i++) {
            TestClass obj = new TestClass();
            obj.setBool(false);
            obj.setMillis(System.currentTimeMillis());
            String name = "name-" + i;
            String key = "key-" + i;
            obj.setString(name);
            obj.setUrl(new URL("http://test.com"));

            StringField field = new StringField("identifier", name);
            kVStore.setValue(TABLE, key, obj, field);
        }
        Query query = new Query();
        StringField field = new StringField("identifier", "name-50");
        query.push(field, Query.Math.EQ);

        List<String> keys = kVStore.search(TABLE, query);
        Assert.assertEquals(1, keys.size());

        Assert.assertEquals(keys.get(0), "key-50");
        for (int i = 0; i < 100; i++) {
            kVStore.deleteValue(TABLE, "key-" + i);
        }
    }

    @Test(enabled = false)
    public void testWithLimitAndOffset() throws MalformedURLException, KVStoreException {
        for (int i = 0; i < 100; i++) {
            TestClass obj = new TestClass();
            obj.setBool(false);
            obj.setMillis(System.currentTimeMillis());
            String name = "name-" + i;
            obj.setString(name);
            obj.setUrl(new URL("http://test.com"));

            StringField field = new StringField("boolean", "true");
            kVStore.setValue(TABLE, name, obj, field);
        }

        Query q = new Query();
        q.push(new StringField("boolean", "true"), Query.Math.EQ);

        kVStore.search(TABLE, q, 10, 0);
        kVStore.search(TABLE, q, 10, 5);

        for (int i = 0; i < 100; i++) {
            kVStore.deleteValue(TABLE, "name-" + i);
        }
    }

}
