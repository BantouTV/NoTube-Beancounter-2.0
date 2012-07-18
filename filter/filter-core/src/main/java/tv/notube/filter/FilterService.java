package tv.notube.filter;

import java.util.Set;

import tv.notube.commons.model.activity.ResolvedActivity;

public interface FilterService {
    void reloadFilters();
    Set<String> processActivity(ResolvedActivity resolvedActivity);
}
