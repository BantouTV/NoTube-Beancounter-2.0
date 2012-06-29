package tv.notube.activities;

import com.google.inject.AbstractModule;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TestActivityStoreModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ActivityStore.class).to(MockActivityStore.class);
    }
}
