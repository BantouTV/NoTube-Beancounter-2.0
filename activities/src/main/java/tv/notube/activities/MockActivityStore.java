package tv.notube.activities;

import org.joda.time.DateTime;
import tv.notube.commons.model.activity.*;
import tv.notube.commons.tests.Tests;
import tv.notube.commons.tests.TestsBuilder;
import tv.notube.commons.tests.TestsException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class MockActivityStore implements ActivityStore {

    private Collection<Activity> activities;

    private Activity lastActivity;

    @Override
    public void store(UUID userId, Activity activity)
            throws ActivityStoreException {
        lastActivity = activity;
        if(activities==null) {
            activities = new ArrayList<Activity>();
        }
        activities.add(activity);
    }

    @Override
    public void store(UUID userId, Collection<Activity> activities)
            throws ActivityStoreException {}

    @Override
    public Collection<Activity> getByUser(UUID uuidId, int max)
            throws ActivityStoreException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public Collection<Activity> getByUserAndDateRange(UUID uuid, DateTime from, DateTime to)
            throws ActivityStoreException {
        // missing the VerbRandomiser -> cannot create Random activities
        Activity a1 = new Activity();
        a1.setVerb(Verb.TWEET);
        a1.setContext(new Context());
        a1.setObject(new Tweet());
        Activity a2 = new Activity();
        a2.setVerb(Verb.LIKE);
        a2.setContext(new Context());
        a2.setObject(new Song());
        Collection<Activity> activities = new ArrayList<Activity>();
        activities.add(a1);
        activities.add(a2);
        return activities;
    }

    @Override
    public Map<UUID, Collection<Activity>> getByDateRange(DateTime from, DateTime to)
            throws ActivityStoreException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public Collection<Activity> getByUser(UUID userId) throws ActivityStoreException {
        if (activities == null) {
            Collection<Activity> allActivites = new ArrayList<Activity>();
            try {
                for (int i = 0; i < 50; i++) {
                    allActivites.add(createFakeActivity(i));
                }
            } catch (TestsException e) {
                throw new ActivityStoreException("Error while creating fakes activities!", e);
            }
            activities = allActivites;
        }
        return activities;
    }

    @Override
    public Activity getByUser(UUID userId, UUID activityId) throws ActivityStoreException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public Collection<Activity> getByUser(UUID userId, Collection<UUID> activityIds) throws ActivityStoreException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void shutDown() throws ActivityStoreException {}

    public Activity getLastActivity() {
        return lastActivity;
    }

    private Activity createFakeActivity(int i) throws TestsException {
        Activity activity = new Activity();
        Tweet tweet = new Tweet();
        tweet.setText("Fake text #" + i);
        Context context = new Context();
        context.setUsername("username");
        context.setDate(new DateTime());
        context.setService("twitter");
        activity.setVerb(Verb.TWEET);
        activity.setId(UUID.randomUUID());
        activity.setContext(context);
        activity.setObject(tweet);
        return activity;
    }

}