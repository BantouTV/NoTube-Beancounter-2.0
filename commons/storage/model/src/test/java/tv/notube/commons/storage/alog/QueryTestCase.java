package tv.notube.commons.storage.alog;

import org.testng.Assert;
import org.testng.annotations.Test;
import tv.notube.commons.storage.model.QueryException;
import tv.notube.commons.storage.model.fields.IntegerField;
import tv.notube.commons.storage.model.fields.StringField;
import tv.notube.commons.storage.model.Query;

/**
 * Unit test for simple App.
 */
public class QueryTestCase {

    @Test(enabled = false)
    public void testCompile() {
        final String expected = "stringfield.name = 'name' AND stringfield" +
                ".value = 'Davide' OR integerfield.name = 'age' AND " +
                "integerfield.value > 31";
        Query query = new Query();
        StringField field1 = new StringField("name", "Davide");
        IntegerField field2 = new IntegerField("age", 31);
        query.push(field1, Query.Math.EQ);
        query.push(Query.Boolean.OR);
        query.push(field2, Query.Math.GT);
        Assert.assertEquals(query.compile(), expected);
    }

    @Test(enabled = false)
    public void testDecompile() throws QueryException {
        final String actual = "integer.newRecommendations = 0 OR     string" +
                ".name =       Mazzini";
        Query query = new Query();
        Query.decompile(actual, query);
        System.out.println(query);
    }

}
