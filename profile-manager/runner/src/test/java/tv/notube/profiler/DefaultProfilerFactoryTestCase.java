package tv.notube.profiler;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class DefaultProfilerFactoryTestCase {

    private DefaultProfilerImpl profiler;

    @BeforeTest(enabled = false)
    public void setUp() {
        profiler = DefaultProfilerFactory.getInstance().build();
    }

    @Test(enabled = false)
    public void test() {
        Assert.assertNotNull(profiler);
    }

}
