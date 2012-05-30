package tv.notube.profiler;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class DefaultProfilerImplTestCase {

    private DefaultProfilerImpl profiler;

    @BeforeTest
    public void setUp() {
        profiler = DefaultProfilerFactory.getInstance().build();
    }

    @Test(enabled = false)
    public void test() throws ProfilerException {
        profiler.run(UUID.fromString("e651ce76-b043-4f0f-a797-29a785f39084"));
    }



}
