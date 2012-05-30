package tv.notube.crawler.requester;

import tv.notube.commons.model.Service;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.auth.Auth;
import tv.notube.commons.tests.RandomBean;
import tv.notube.commons.tests.Tests;
import tv.notube.commons.tests.TestsBuilder;
import tv.notube.commons.tests.TestsException;
import tv.notube.commons.tests.randomisers.IntegerRandomiser;
import tv.notube.crawler.requester.Requester;
import tv.notube.crawler.requester.RequesterException;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class MockRequester implements Requester {

    private Tests tests = TestsBuilder.getInstance().build();

    @Override
    public List<Activity> call(Service service, Auth auth) throws RequesterException {
        List<Activity> activities = new ArrayList<Activity>();
        int noa = (new IntegerRandomiser("anom", 100)).getRandom();
        try {
            for(RandomBean<Activity> rb : tests.build(Activity.class, noa)) {
                activities.add(rb.getObject());
            }
        } catch (TestsException e) {
            throw new RequesterException("Error while building random activities for service [" + service.getName() + "]", e);
        }
        return activities;
    }
}