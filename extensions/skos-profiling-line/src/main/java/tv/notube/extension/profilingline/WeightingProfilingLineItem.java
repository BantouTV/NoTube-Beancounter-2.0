package tv.notube.extension.profilingline;

import sun.nio.ch.SocketOpts;
import tv.notube.commons.model.Interest;
import tv.notube.commons.model.UserProfile;
import tv.notube.commons.model.activity.Activity;
import tv.notube.profiler.line.ProfilingLineItem;
import tv.notube.profiler.line.ProfilingLineItemException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class WeightingProfilingLineItem extends ProfilingLineItem {

    private static URL FACEBOOK;

    private static URL TWITTER;

    private static URL LASTFM;

    private static URL IPLAYER;

    public WeightingProfilingLineItem(String name, String description) {
        super(name, description);
        try {
            FACEBOOK = new URL("http://facebook.com");
            TWITTER = new URL("http://twitter.com");
            LASTFM = new URL("http://lastfm.com");
            IPLAYER = new URL("http://www.bbc.co.uk/iplayer/");
        } catch (MalformedURLException e) {
            final String errMsg = "Malformed url";
            throw new RuntimeException(errMsg, e);
        }
    }

    @Override
    public void execute(Object o) throws ProfilingLineItemException {
        RawData intermediate = (RawData) o;
        Map<Activity, List<URI>> linkedActivities
                = intermediate.getLinkedActivities();

        Map<URI, List<Activity>> activitiesByInterest
                = new HashMap<URI, List<Activity>>();

        for (Activity activity : linkedActivities.keySet()) {
            List<URI> activityInterests = linkedActivities.get(activity);
            for(URI activityInterest : activityInterests) {
                if(activitiesByInterest.containsKey(activityInterest)) {
                    activitiesByInterest.get(activityInterest).add(activity);
                } else {
                    List<Activity> interestActivities =
                            new ArrayList<Activity>();
                    interestActivities.add(activity);
                    activitiesByInterest.put(
                            activityInterest,
                            interestActivities
                    );
                }
            }
        }
        Set<Interest> wInterests = new HashSet<Interest>();
        int activitiesNumber = linkedActivities.keySet().size();
        double initialWeight = 1.0d / activitiesNumber;
        for(URI interest : activitiesByInterest.keySet()) {
            Interest interestObj = new Interest();
            interestObj.setResource(interest);
            interestObj.setVisible(true);
            List<Activity> uniqActivitites = uniq(
                    activitiesByInterest.get(interest)
            );
            double w = initialWeight * getMultiplier(uniqActivitites);
            interestObj.setWeight(w);
            interestObj.setActivities(uniqActivitites);
            wInterests.add(interestObj);
        }
        WeightedInterests weightedInterests = new WeightedInterests(
                intermediate.getUsername(),
                normalize(wInterests)
                );
        this.getNextProfilingLineItem().execute(weightedInterests);
    }

    private double getMultiplier(List<Activity> uniqActivitites) {
        double multiplier = 1.0d;
        for (Activity a : uniqActivitites) {
            URL service = a.getContext().getService();
            if (service.equals(FACEBOOK)) {
                multiplier += 3.5d;
            }
            if (service.equals(TWITTER)) {
                multiplier += 1.5d;
            }
            if (service.equals(LASTFM)) {
                multiplier += 0.7d;
            }
            if (service.equals(IPLAYER)) {
                multiplier += 3.5d;
            }
        }
        return multiplier;
    }

    private List<Activity> uniq(List<Activity> activities) {
        Set<Activity> unique = new HashSet<Activity>(activities);
        return Arrays.asList(unique.toArray(new Activity[unique.size()]));
    }

    private Set<Interest> normalize(Set<Interest> wInterests) {
        // a = avarage
        // took only the ones over the avarage
        double a = 0.0d;
        for(Interest i : wInterests) {
            a += i.getWeight();
        }
        a = a / (double) wInterests.size();
        Set<Interest> overs = new HashSet<Interest>();
        int activitiesNumber = 0;
        for (Interest i : wInterests) {
            if(i.getWeight() >= a) {
                overs.add(i);
                activitiesNumber += i.getActivities().size();
            }
        }
        double initialWeight = 1.0d / (double) activitiesNumber;
        Set<Interest> result = new HashSet<Interest>();
        for(Interest i : overs) {
            i.setWeight(initialWeight * i.getActivities().size());
            result.add(i);
        }
        return result;
    }
}
