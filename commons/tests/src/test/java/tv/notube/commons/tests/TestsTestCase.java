package tv.notube.commons.tests;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import tv.notube.commons.tests.model.FakeBean;
import tv.notube.commons.tests.model.FakeBeanWithDate;
import tv.notube.commons.tests.model.RecursiveBean;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TestsTestCase {

    private Tests tests;

    private static final int FROM = 5;

    private static final int TO = 10;

    @BeforeTest
    public void setUp() {
        tests = TestsBuilder.getInstance().build();
        tests.register(new RecursiveBeanRandomiser("rb-randomizer"));
    }

    @Test
    public void testWithSimpleFakeBean() throws TestsException {
        RandomBean<FakeBean> rb = tests.build(FakeBean.class);
        FakeBean actual = rb.getObject();
        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getString(),   rb.getValue("string"));
        Assert.assertEquals(actual.getInteger(),  rb.getValue("integer"));
        Assert.assertTrue(actual.getInteger() <= TO && actual.getInteger() >= FROM);
        Assert.assertEquals(actual.getFakePoint(), rb.getValues().get("fakePoint"));
    }

    @Test
    public void testWithRecursiveBean() throws TestsException {
        RandomBean<RecursiveBean> rb = tests.build(RecursiveBean.class);
        RecursiveBean actual = rb.getObject();
        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getUrl(), rb.getValue("url"));
        Assert.assertEquals(actual.getBean(),  rb.getValue("bean"));
    }

    @Test
    public void testWithJodaDateTime() throws TestsException {
        RandomBean<FakeBeanWithDate> rb = tests.build(FakeBeanWithDate.class);
        FakeBeanWithDate actual = rb.getObject();
        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getDate(), rb.getValue("date"));
    }

    @Test
    public void testAssert() throws TestsException {
        RandomBean<FakeBean> rb = tests.build(FakeBean.class);
        tests.assertCompliance(rb);
    }

}
