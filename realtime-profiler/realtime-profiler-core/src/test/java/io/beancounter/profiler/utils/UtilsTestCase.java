package io.beancounter.profiler.utils;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import io.beancounter.commons.model.activity.Activity;
import io.beancounter.commons.model.activity.Verb;
import io.beancounter.commons.tests.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Reference test class for {@link Utils#sortByDate(java.util.List)}
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class UtilsTestCase {

    private List<Activity> activities;

    @BeforeTest
    public void setUp() throws TestsException {
        // randomly instantiate only 10 activities
        activities = getActivities(10);
    }

    private List<Activity> getActivities(int size) throws TestsException {
        Tests tests = TestsBuilder.getInstance().build();
        tests.register(new Randomiser<Verb>() {
            @Override
            public Class<Verb> type() {
                return Verb.class;
            }

            @Override
            public String name() {
                return "verb-randomiser";
            }

            @Override
            public Verb getRandom() {
                return Verb.TWEET;
            }
        });
        Collection<RandomBean<Activity>> activities = tests.build(Activity.class, size);
        List<Activity> result = new ArrayList<Activity>();
        for(RandomBean<Activity> rb : activities) {
            result.add(rb.getObject());
        }
        return result;
    }

    @Test
    public void testSortByDate() {
        Assert.assertFalse(sorted(activities));
        Utils.sortByDate(activities);
        Assert.assertTrue(sorted(activities));

    }

    private boolean sorted(List<Activity> activities) {
        Activity current = activities.get(0);
        for(int i = 1; i < activities.size(); i++) {
            Activity activity = activities.get(i);
            if(current.getContext().getDate().compareTo(activity.getContext().getDate()) < 1) {
                return false;
            }
            current = activity;
            i++;
        }
        return true;
    }

}
