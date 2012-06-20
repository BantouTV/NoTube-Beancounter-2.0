package tv.notube.profiler.utils;

import tv.notube.commons.model.Interest;

import java.net.URI;
import java.util.*;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Utils {

    public static boolean contains(Interest i, Collection<Interest> interests) {
        for(Interest interest : interests) {
            if(interest.equals(i))
                return true;
        }
        return false;
    }

    public static Interest retrieve(URI resource, Set<Interest> oldInterests) {
        for(Interest interest : oldInterests) {
            if(resource.equals(interest.getResource()))
                return interest;
        }
        return null;
    }

    public static Interest merge(Interest nu, Interest old) {
        for(UUID activityId : nu.getActivities()) {
            old.addActivity(activityId);
        }
        old.setWeight(old.getWeight() + nu.getWeight());
        return old;
    }

    public static List<Interest> union(
            Collection<Interest> newInterests,
            Collection<Interest> oldInterests
    ) {
        Collection<Interest> union = new HashSet<Interest>();
        union.addAll(newInterests);
        union.addAll(oldInterests);
        return new ArrayList<Interest>(union);
    }

    public static List<Interest> cut(List<Interest> union, int cut) {
        List<Interest> result = new ArrayList<Interest>();
        for(int i=0; i < cut; i++) {
            result.add(union.get(i));
        }
        return result;
    }
}
