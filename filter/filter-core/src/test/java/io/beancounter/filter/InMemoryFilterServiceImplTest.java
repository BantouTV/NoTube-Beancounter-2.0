package io.beancounter.filter;

import io.beancounter.commons.model.activity.ResolvedActivity;
import io.beancounter.filter.manager.FilterManager;
import io.beancounter.filter.model.Filter;
import io.beancounter.filter.model.pattern.ActivityPattern;
import org.mockito.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class InMemoryFilterServiceImplTest {

    private FilterManager filterManager;
    private InMemoryFilterServiceImpl filterService;

    @BeforeMethod
    public void setUp() throws Exception {
        filterManager = mock(FilterManager.class);
        filterService = new InMemoryFilterServiceImpl(filterManager);
    }

    @Test
    public void processingActivityWithNoFiltersShouldReturnNoQueues() throws Exception {
        Set<String> queues = filterService.processActivity(new ResolvedActivity());
        assertTrue(queues.isEmpty());
    }

    @Test
    public void processingActivityWithOneFilterGivenActivityMatchesShouldReturnThatFiltersQueues() throws Exception {
        String filterName = "name";
        ActivityPattern pattern = mock(ActivityPattern.class);
        Set<String> queues = new HashSet<String>();
        queues.add("queue1");
        queues.add("queue2");
        Filter filter = new Filter(filterName, "description", pattern, queues);
        filter.setActive(true);

        when(pattern.matches(Matchers.<ResolvedActivity>any())).thenReturn(true);
        when(filterManager.get(filterName)).thenReturn(filter);

        filterService.refresh(filterName);
        Set<String> sendingQueues = filterService.processActivity(new ResolvedActivity());

        assertEquals(sendingQueues, queues);
    }

    @Test
    public void processingActivityWithOneFilterGivenActivityDoesNotMatchShouldReturnNoQueues() throws Exception {
        String filterName = "name";
        ActivityPattern pattern = mock(ActivityPattern.class);
        Set<String> queues = new HashSet<String>();
        queues.add("queue1");
        queues.add("queue2");
        Filter filter = new Filter(filterName, "description", pattern, queues);
        filter.setActive(true);

        when(pattern.matches(Matchers.<ResolvedActivity>any())).thenReturn(false);
        when(filterManager.get(filterName)).thenReturn(filter);

        filterService.refresh(filterName);
        Set<String> sendingQueues = filterService.processActivity(new ResolvedActivity());

        assertTrue(sendingQueues.isEmpty());
    }

    @Test
    public void processingActivityWithTwoFiltersGivenActivityMatchesShouldReturnBothFiltersQueues() throws Exception {
        ActivityPattern pattern = mock(ActivityPattern.class);

        String filterName1 = "name-1";
        Set<String> queues1 = new HashSet<String>();
        queues1.add("queue1");
        queues1.add("queue2");
        Filter filter1 = new Filter(filterName1, "description", pattern, queues1);
        filter1.setActive(true);

        String filterName2 = "name-2";
        Set<String> queues2 = new HashSet<String>();
        queues2.add("queue2");
        queues2.add("queue3");
        Filter filter2 = new Filter(filterName2, "description", pattern, queues2);
        filter2.setActive(true);

        when(pattern.matches(Matchers.<ResolvedActivity>any())).thenReturn(true);
        when(filterManager.get(filterName1)).thenReturn(filter1);
        when(filterManager.get(filterName2)).thenReturn(filter2);

        filterService.refresh(filterName1);
        filterService.refresh(filterName2);
        Set<String> sendingQueues = filterService.processActivity(new ResolvedActivity());

        Set<String> expectedQueues = new HashSet<String>();
        expectedQueues.add("queue1");
        expectedQueues.add("queue2");
        expectedQueues.add("queue3");
        assertEquals(sendingQueues, expectedQueues);
    }
}
