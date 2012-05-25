package tv.notube.activities;

import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Guice
public class ElasticSearchActivityStoreImplTestCase {

    private ActivityStore as;

    @Test
    public void testCRUD() {
        final UUID userId = UUID.randomUUID();
    }

}
