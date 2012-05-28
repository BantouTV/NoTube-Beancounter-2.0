package tv.notube.commons.tests;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class BuilderTestCase {

    private Builder builder;

    private static final int FROM = 5;

    private static final int TO = 10;

    @BeforeTest
    public void setUp() {
        builder = new Builder();
        builder.register(new StringRandomiser("string-randomizer", 2, 15, false));
        builder.register(new IntegerRandomiser("int-randomizer", FROM, TO));
        builder.register(new DoubleRandomiser("double-randomizer", FROM, TO));
        builder.register(new UUIDRandomiser("uuid-randomizer"));
    }

    @Test
    public void testWithSimpleFakeBean() throws BuilderException {
        RandomBean<FakeBean> rb = builder.build(FakeBean.class);
        FakeBean actual = rb.getObject();
        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getString(),   rb.getValue("string"));
        Assert.assertEquals(actual.getInteger(),  rb.getValue("integer"));
        Assert.assertTrue(actual.getInteger() <= TO && actual.getInteger() >= FROM);
        Assert.assertEquals(actual.getFakePoint(), rb.getValues().get("fakePoint"));
    }

}
