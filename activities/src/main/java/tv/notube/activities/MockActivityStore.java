package tv.notube.activities;

import org.joda.time.DateTime;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.Context;
import tv.notube.commons.model.activity.Song;
import tv.notube.commons.model.activity.Tweet;
import tv.notube.commons.model.activity.Verb;
import tv.notube.commons.model.activity.rai.TVEvent;
import tv.notube.commons.tests.TestsException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    public Activity getByUser(UUID userId, UUID activityId) throws ActivityStoreException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public Collection<Activity> getByUser(UUID userId, Collection<UUID> activityIds) throws ActivityStoreException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public Collection<Activity> getByUserPaginated(
            UUID userId, int pageNumber, int size
    ) throws ActivityStoreException {
        // User with no activities.
        if ("0ad77722-1338-4c32-9209-5b952530959d".equals(userId.toString())) {
            return new ArrayList<Activity>();
        }

        int startAt = pageNumber * size;

        List<Activity> allActivities = new ArrayList<Activity>();
        try {
            for (int i = 0; i < 50; i++) {
                allActivities.add(createFakeActivity(i));
            }
        } catch (TestsException e) {
            throw new ActivityStoreException("Error while creating fake activities!", e);
        }

        Collection<Activity> results = new ArrayList<Activity>();
        for (int i = startAt; i < startAt + size && i < allActivities.size(); i++) {
            results.add(allActivities.get(i));
        }

        return results;
    }

    @Override
    public void shutDown() throws ActivityStoreException {}

    @Override
    public Collection<Activity> search(
            String path, String value, int pageNumber, int size
    ) throws ActivityStoreException, WildcardSearchException {
        if (path.contains("*") || value.contains("*")) {
            throw new WildcardSearchException("Wildcard searches are not allowed.");
        }

        List<Activity> allActivities = new ArrayList<Activity>();

        if (value.equals("RAI-CONTENT-ITEM")) {
            try {
                allActivities.add(createCustomActivity());
            } catch (Exception ex) {
                throw new ActivityStoreException("Error while creating fake activities!", ex);
            }
        } else {
            try {
                for (int i = 0; i < 50; i++) {
                    allActivities.add(createFakeActivity(i));
                }
            } catch (Exception ex) {
                throw new ActivityStoreException("Error while creating fake activities!", ex);
            }
        }

        int startAt = pageNumber * size;
        Collection<Activity> results = new ArrayList<Activity>();

        for (int i = startAt; i < startAt + size && i < allActivities.size(); i++) {
            results.add(allActivities.get(i));
        }

        return results;
    }

    public Activity getLastActivity() {
        return lastActivity;
    }

    private Activity createFakeActivity(int i) throws TestsException {
        Activity activity = new Activity();
        Tweet tweet = new Tweet();
        tweet.setText("Fake text #" + i);
        Context context = new Context();
        context.setUsername("username");
        context.setDate(new DateTime().minusMinutes(i));
        context.setService("twitter");
        activity.setVerb(Verb.TWEET);
        activity.setId(UUID.randomUUID());
        activity.setContext(context);
        activity.setObject(tweet);
        return activity;
    }

    private Activity createCustomActivity() throws Exception {
        Activity activity = new Activity();
        activity.setId(UUID.randomUUID());
        activity.setVerb(Verb.WATCHED);

        TVEvent tvEvent = new TVEvent(UUID.randomUUID(), "Euro 2012", "");

        Context context = new Context();
        context.setUsername("rai-username");
        context.setDate(new DateTime());
        context.setService("rai-tv");

        activity.setContext(context);
        activity.setObject(tvEvent);

        return activity;
    }

}