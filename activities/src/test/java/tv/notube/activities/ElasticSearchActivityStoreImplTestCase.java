package tv.notube.activities;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ElasticSearchActivityStoreImplTestCase {

    private ActivityStore as;

    @BeforeTest
    public void setUp() {
        as = new ElasticSearchActivityStoreImpl();
    }

    @AfterTest
    public void tearDown() {
        as = null;
    }

    @Test
    public void testCRUD() {
        final UUID userId = UUID.randomUUID();
    }

}
