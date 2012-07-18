package tv.notube.filter;

import java.util.Collections;
import java.util.Set;

import tv.notube.commons.model.activity.ResolvedActivity;

public class FilterServiceImpl implements FilterService {

    @Override
    public void reloadFilters() {
        System.out.println("reloading filters");
    }

    @Override
    public Set<String> processActivity(ResolvedActivity resolvedActivity) {
        return Collections.emptySet();
    }
}
