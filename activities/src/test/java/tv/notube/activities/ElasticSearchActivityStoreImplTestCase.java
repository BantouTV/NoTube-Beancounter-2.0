package tv.notube.activities;

import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import tv.notube.commons.model.activity.Activity;

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
        Activity activity = getRandomActivity();
    }

    private Activity getRandomActivity() {

        return null;  //To change body of created methods use File | Settings | File Templates.
    }

}
