package io.beancounter.profiler.utils;

import io.beancounter.commons.model.Interest;
import io.beancounter.commons.model.Topic;
import io.beancounter.commons.model.activity.Activity;

import java.net.URI;
import java.util.*;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Utils {

    public static <T extends Topic> boolean contains(T t, Collection<T> ts) {
        for(T ti : ts) {
            if(ti.equals(t))
                return true;
        }
        return false;
    }

    public static <T extends Topic> T retrieve(URI resource, Set<T> oldTs) {
        for(T t : oldTs) {
            if(resource.equals(t.getResource()))
                return t;
        }
        return null;
    }

    public static <T extends Topic> List<T> union(
            Collection<T> newTopic,
            Collection<T> oldTopic
    ) {
        Collection<T> union = new HashSet<T>();
        union.addAll(newTopic);
        union.addAll(oldTopic);
        return new ArrayList<T>(union);
    }

    public static <T extends Topic> List<T> cut(List<T> union, int cut) {
        List<T> result = new ArrayList<T>();
        for(int i=0; i < cut; i++) {
            result.add(union.get(i));
        }
        return result;
    }

    public static void sortByDate(List<Activity> activities) {
        Collections.sort(activities, new ActivityContextDateComparator());
        Collections.reverse(activities);
    }


    private static class ActivityContextDateComparator implements Comparator<Activity> {
        @Override
        public int compare(Activity object, Activity object1) {
            return object.getContext().getDate().compareTo(
                    object1.getContext().getDate()
            );
        }
    }
}
