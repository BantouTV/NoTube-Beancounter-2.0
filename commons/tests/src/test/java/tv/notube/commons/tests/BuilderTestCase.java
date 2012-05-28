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

    @BeforeTest
    public void setUp() {
        builder = new Builder();
        builder.register(new StringRandomiser("string-randomizer"));
        builder.register(new IntegerRandomiser("int-randomizer"));
    }

    @Test
    public void test() throws BuilderException {
        FakeBean fakeBean = (FakeBean) builder.build(FakeBean.class);
        Assert.assertNotNull(fakeBean);
        Assert.assertEquals(fakeBean.getString(), "random-string");
        Assert.assertEquals(fakeBean.getInteger(), 5);
    }

}
