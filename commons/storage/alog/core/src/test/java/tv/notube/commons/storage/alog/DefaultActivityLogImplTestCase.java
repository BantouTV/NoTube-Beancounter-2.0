package tv.notube.commons.storage.alog;

import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.storage.model.fields.*;
import tv.notube.commons.storage.model.fields.serialization.SerializationManager;
import tv.notube.commons.storage.model.fields.serialization.SerializationManagerException;
import tv.notube.commons.storage.model.Activity;
import tv.notube.commons.storage.model.ActivityLog;
import tv.notube.commons.storage.model.ActivityLogException;
import tv.notube.commons.storage.model.Query;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class DefaultActivityLogImplTestCase {

    private ActivityLog activityLog;

    private static final String OWNER = "test-owner";

    @BeforeTest
    public void setUp() throws ActivityLogException {
        Properties properties = new Properties();
        properties.setProperty("url", "jdbc:mysql://127.0.0.1:3306/alog");
        properties.setProperty("username", "alog");
        properties.setProperty("password", "alog");
        activityLog = new DefaultActivityLogImpl(properties);
    }

    @AfterTest
    public void tearDown() throws ActivityLogException {
        activityLog.delete(OWNER);
    }

    @Test(enabled = false)
    public void simpleTest() throws ActivityLogException {
        DateTime before = new DateTime();
        for(int i=0; i < 10; i++) {
            IntegerField index = new IntegerField("index", i);
            Field fields[] = { index };
            activityLog.log(OWNER, "just a test activity", fields);
        }
        DateTime now = new DateTime();
        Activity activities[] = activityLog.filter(before, now);
        Assert.assertEquals(10, activities.length);

        activities = activityLog.filter(before, now, OWNER);
        Assert.assertEquals(10, activities.length);

        for(Activity activity : activities) {
            Field[] fields = activityLog.getFields(activity.getId());
            Assert.assertEquals(1, fields.length);
        }
        activityLog.delete(OWNER);
        activities = activityLog.filter(before, now);
        Assert.assertEquals(0, activities.length);
    }

    @Test(enabled = false)
    public void testQueryOnFields() throws ActivityLogException {
        DateTime before = new DateTime();
        for(int i=0; i < 10; i++) {
            IntegerField index = new IntegerField("index", i);
            StringField name = new StringField("name", "name" + i);
            Field fields[] = { index, name };
            activityLog.log(OWNER, "just a test activity", fields);
        }
        DateTime after = new DateTime();
        Query query = new Query();
        query.push(new IntegerField("index", 4), Query.Math.GT);
        Activity activities[] = activityLog.filter(
                before,
                after,
                OWNER,
                query
        );
        Assert.assertEquals(5, activities.length);

        query = new Query();
        query.push(new IntegerField("index", 4), Query.Math.GT);
        query.push(Query.Boolean.AND);
        query.push(new StringField("name", "name5"), Query.Math.EQ);
        activities = activityLog.filter(
                before,
                after,
                OWNER,
                query
        );
        Assert.assertEquals(1, activities.length);
        Field fields[] = activityLog.getFields(
                activities[0].getId()
        );
        Assert.assertEquals(2, fields.length);

        activityLog.delete(OWNER);
        activities = activityLog.filter(before, after);
        Assert.assertEquals(activities.length, 0);
    }

    @Test(enabled = false)
    public void testQueryOnFieldsWithDate() throws ActivityLogException {
        DateTime before = new DateTime();
        for(int i=0; i < 10; i++) {
            IntegerField index = new IntegerField("index", i);
            StringField name = new StringField("name", "name" + i);
            Field fields[] = { index, name };
            activityLog.log(OWNER, "just a test activity", fields);
        }
        Query query = new Query();
        query.push(new IntegerField("index", 4), Query.Math.GT);
        DateTime after = new DateTime();
        Activity activities[] = activityLog.filter(
                after,
                OWNER,
                query
        );
        Assert.assertEquals(5, activities.length);
        activities = activityLog.filter(after, OWNER);
        Assert.assertEquals(10, activities.length);

        activityLog.delete(OWNER);
        activities = activityLog.filter(before, after);
        Assert.assertEquals(activities.length, 0);
    }

    @Test(enabled = false)
    public void testOnSimpleFields()
            throws MalformedURLException, ActivityLogException {
        DateTime before = new DateTime();
        DatetimeField dateTimeField = new DatetimeField(
                "date",
                new DateTime()
        );
        URLField urlField = new URLField(
                "url",
                new URL("http://test.com/index.html")
        );
        Field fields[] = { dateTimeField, urlField };
        activityLog.log(OWNER, "just a test activity", fields);
        DateTime after = new DateTime();
        Activity activities[] = activityLog.filter(before, after, OWNER);
        Assert.assertEquals(1, activities.length);

        Field actual[] = activityLog.getFields(activities[0].getId());
        Assert.assertEquals(2, actual.length);

        activityLog.delete(OWNER);
        activities = activityLog.filter(before, after, OWNER);
        Assert.assertEquals(0, activities.length);

        activityLog.delete(OWNER);
        activities = activityLog.filter(before, after);
        Assert.assertEquals(activities.length, 0);
    }

    @Test(enabled = false)
    public void testDeleteByDateRange()
            throws ActivityLogException, InterruptedException {
        DateTime before = new DateTime();
        for (int i = 0; i < 10; i++) {
            IntegerField index = new IntegerField("index", i);
            Field fields[] = {index};
            activityLog.log(OWNER, "just a test activity", fields);
        }
        DateTime after = new DateTime();
        Activity activities[] = activityLog.filter(before, after,
                OWNER);
        Assert.assertEquals(activities.length, 10);

        activityLog.delete(before, after);
        activities = activityLog.filter(before, after,
                OWNER);
        Assert.assertEquals(activities.length, 0);

        activityLog.delete(OWNER);
        activities = activityLog.filter(before, after);
        Assert.assertEquals(activities.length, 0);
    }

    @Test(enabled = false)
    public void testSerializableObjects()
            throws ActivityLogException, SerializationManagerException {
        DateTime before = new DateTime();
        TestClass expected = new TestClass();
        SerializationManager sm = new SerializationManager();
        Bytes bytes = sm.serialize(expected);
        BytesField field = new BytesField("object", bytes);
        Field fields[] = { field };
        activityLog.log(OWNER, "just a test activity", fields);
        DateTime after = new DateTime();
        Activity activities[] = activityLog.filter(before, after, OWNER);

        Assert.assertEquals(1, activities.length);
        Field actualFields[] = activityLog.getFields(activities[0].getId());
        Assert.assertEquals(1, actualFields.length);

        Assert.assertEquals(field, actualFields[0]);

        activityLog.delete(OWNER);
        activities = activityLog.filter(before, after);
        Assert.assertEquals(activities.length, 0);
    }

    @Test(enabled = false)
    public void testDeleteByDateRangeAndOwner() throws ActivityLogException {
        DateTime before = new DateTime();
        for (int i = 0; i < 10; i++) {
            IntegerField index = new IntegerField("index", i);
            Field fields[] = {index};
            activityLog.log(OWNER, "just a test activity", fields);
        }
        DateTime after = new DateTime();
        Activity activities[] = activityLog.filter(
                before,
                after,
                OWNER);
        Assert.assertEquals(activities.length, 10);

        activityLog.delete(before, after, OWNER);
        activities = activityLog.filter(
                before,
                after,
                OWNER);
        Assert.assertEquals(activities.length, 0);

        activityLog.delete(OWNER);
        activities = activityLog.filter(before, after);
        Assert.assertEquals(activities.length, 0);
    }

    @Test(enabled = false)
    public void testMultipleFields() throws MalformedURLException, ActivityLogException {
        DateTime before = new DateTime();
        URLField urlField1 = new URLField(
                "url-1",
                new URL("http://test.com/index-2.html")
        );
        URLField urlField2 = new URLField(
                "url-2",
                new URL("http://test.com/index-2.html")
        );
        activityLog.log(OWNER, "multiple fields", urlField1, urlField2);
        DateTime after = new DateTime();
        Activity activities[] = activityLog.filter(before, after);
        Assert.assertEquals(1, activities.length);
        Field fields[] = activityLog.getFields(activities[0].getId());
        Assert.assertEquals(2, fields.length);

        activityLog.delete(OWNER);
        activities = activityLog.filter(before, after);
        Assert.assertEquals(activities.length, 0);
    }

}