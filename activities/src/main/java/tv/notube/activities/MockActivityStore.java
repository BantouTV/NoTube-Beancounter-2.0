package tv.notube.activities;

import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import tv.notube.commons.model.User;
import tv.notube.commons.model.activity.*;
import tv.notube.commons.model.activity.rai.TVEvent;
import tv.notube.commons.model.auth.OAuthAuth;
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

    private Collection<ResolvedActivity> activities;

    private ResolvedActivity lastActivity;

    @Override
    public void store(UUID userId, ResolvedActivity activity)
            throws ActivityStoreException {
        lastActivity = activity;
        if(activities==null) {
            activities = new ArrayList<ResolvedActivity>();
        }
        activities.add(activity);
    }

    @Override
    public void store(UUID userId, Collection<ResolvedActivity> activities)
            throws ActivityStoreException {}

    @Override
    public Collection<ResolvedActivity> getByUser(UUID uuidId, int max)
            throws ActivityStoreException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public Collection<ResolvedActivity> getByUserAndDateRange(UUID uuid, DateTime from, DateTime to)
            throws ActivityStoreException {
        // missing the VerbRandomiser -> cannot create Random activities
        Activity a1 = new Activity();
        a1.setVerb(Verb.TWEET);
        a1.setContext(new Context());
        a1.setObject(new Tweet());
        ResolvedActivity ra1 = new ResolvedActivity();
        ra1.setActivity(a1);
        ra1.setUser(getUser());
        ra1.setUserId(UUID.randomUUID());
        Activity a2 = new Activity();
        a2.setVerb(Verb.LIKE);
        a2.setContext(new Context());
        a2.setObject(new Song());
        ResolvedActivity ra2 = new ResolvedActivity();
        ra2.setActivity(a2);
        ra2.setUser(getUser());
        ra2.setUserId(UUID.randomUUID());
        Collection<ResolvedActivity> activities = new ArrayList<ResolvedActivity>();
        activities.add(ra1);
        activities.add(ra2);
        return activities;
    }

    private User getUser() {
        User user = new User();
        user.setSurname("test-surname");
        user.setName("test-name");
        user.setPassword("test-password");
        user.setUsername("test-username");
        user.addService("test-service", new OAuthAuth("s", "c"));
        return user;
    }

    @Override
    public Map<UUID, Collection<ResolvedActivity>> getByDateRange(DateTime from, DateTime to)
            throws ActivityStoreException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public ResolvedActivity getByUser(UUID userId, UUID activityId) throws ActivityStoreException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public Collection<ResolvedActivity> getByUser(UUID userId, Collection<UUID> activityIds) throws ActivityStoreException {
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public Collection<ResolvedActivity> getByUserPaginated(
            UUID userId, int pageNumber, int size, String order
    ) throws ActivityStoreException, InvalidOrderException {
        // User with no activities.
        if ("0ad77722-1338-4c32-9209-5b952530959d".equals(userId.toString())) {
            return new ArrayList<ResolvedActivity>();
        }

        int startAt = pageNumber * size;

        List<ResolvedActivity> allActivities = new ArrayList<ResolvedActivity>();
        try {
            if (SortOrder.DESC.toString().equals(order)) {
                for (int i = 0; i < 50; i++) {
                    allActivities.add(createFakeActivity(i));
                }
            } else if (SortOrder.ASC.toString().equals(order)) {
                for (int i = 49; i >= 0; i--) {
                    allActivities.add(createFakeActivity(i));
                }
            } else {
                throw new InvalidOrderException(order + " is not a valid sort order.");
            }
        } catch (TestsException e) {
            throw new ActivityStoreException("Error while creating fake activities!", e);
        }

        Collection<ResolvedActivity> results = new ArrayList<ResolvedActivity>();
        for (int i = startAt; i < startAt + size && i < allActivities.size(); i++) {
            results.add(allActivities.get(i));
        }

        return results;
    }

    @Override
    public void shutDown() throws ActivityStoreException {}

    @Override
    public Collection<ResolvedActivity> search(
            String path, String value, int pageNumber, int size, String order
    ) throws ActivityStoreException, WildcardSearchException, InvalidOrderException {
        if (path.contains("*") || value.contains("*")) {
            throw new WildcardSearchException("Wildcard searches are not allowed.");
        }

        List<ResolvedActivity> allActivities = new ArrayList<ResolvedActivity>();

        if (value.equals("RAI-CONTENT-ITEM")) {
            try {
                allActivities.add(createCustomActivity());
            } catch (Exception ex) {
                throw new ActivityStoreException("Error while creating fake activities!", ex);
            }
        } else {
            try {
                if (SortOrder.DESC.toString().equals(order)) {
                    for (int i = 0; i < 50; i++) {
                        allActivities.add(createFakeActivity(i));
                    }
                } else if (SortOrder.ASC.toString().equals(order)) {
                    for (int i = 49; i >= 0; i--) {
                        allActivities.add(createFakeActivity(i));
                    }
                } else {
                    throw new InvalidOrderException(order + " is not a valid sort order.");
                }
            } catch (TestsException ex) {
                throw new ActivityStoreException("Error while creating fake activities!", ex);
            }
        }

        int startAt = pageNumber * size;
        Collection<ResolvedActivity> results = new ArrayList<ResolvedActivity>();

        for (int i = startAt; i < startAt + size && i < allActivities.size(); i++) {
            results.add(allActivities.get(i));
        }

        return results;
    }

    @Override
    public void setVisible(UUID activityId, boolean visible) throws ActivityStoreException {
        throw new UnsupportedOperationException();
    }

    private ResolvedActivity createFakeActivity(int i) throws TestsException {
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
        return new ResolvedActivity(UUID.randomUUID(), activity, getUser());
    }

    private ResolvedActivity createCustomActivity() throws Exception {
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

        return new ResolvedActivity(UUID.randomUUID(), activity, getUser());
    }

}