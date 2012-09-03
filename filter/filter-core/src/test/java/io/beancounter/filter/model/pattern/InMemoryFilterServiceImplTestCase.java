package io.beancounter.filter.model.pattern;

import com.google.inject.*;
import io.beancounter.commons.model.activity.*;
import io.beancounter.commons.model.activity.Object;
import io.beancounter.commons.model.activity.rai.Comment;
import io.beancounter.filter.FilterService;
import io.beancounter.filter.FilterServiceException;
import io.beancounter.filter.InMemoryFilterServiceImpl;
import io.beancounter.filter.manager.FilterManager;
import io.beancounter.filter.manager.FilterManagerException;
import io.beancounter.filter.manager.InMemoryFilterManager;
import io.beancounter.filter.model.pattern.rai.CommentPattern;
import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Reference test case for {@link io.beancounter.filter.InMemoryFilterServiceImpl}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class InMemoryFilterServiceImplTestCase {

    private FilterService filterService;

    @BeforeTest
    public void setUp() throws FilterServiceException {
        Injector injector = Guice.createInjector(new FilterTestModule());
        filterService = injector.getInstance(FilterService.class);
        filterService.refresh();
    }

    @AfterTest
    public void tearDown() {}

    @Test
    public void test() throws FilterServiceException {
        ResolvedActivity shouldBeFilteredOut = getSongActivity();
        Set<String> actual = filterService.processActivity(shouldBeFilteredOut);
        Assert.assertEquals(actual.size(), 0);
        ResolvedActivity shouldBeNotFiltered = getRAIComment();
        actual = filterService.processActivity(shouldBeNotFiltered);
        Assert.assertEquals(actual.size(), 1);
        shouldBeNotFiltered = getWatchedActivity();
        Assert.assertEquals(actual.size(), 1);
    }

    private ResolvedActivity getWatchedActivity() {
        Activity activity = new Activity();
        activity.setVerb(Verb.WATCHED);
        Object obj = new Object();
        activity.setObject(obj);
        activity.setContext(new Context(DateTime.now()));
        return new ResolvedActivity(
                UUID.randomUUID(),
                activity,
                null
        );
    }

    private ResolvedActivity getRAIComment() {
        Activity activity = new Activity();
        activity.setVerb(Verb.COMMENT);
        Comment comment = new Comment();
        comment.setOnEvent("event-id");
        activity.setObject(comment);
        activity.setContext(new Context(DateTime.now()));
        return new ResolvedActivity(
                UUID.randomUUID(),
                activity,
                null
        );
    }

    private ResolvedActivity getSongActivity() {
        Activity activity = new Activity();
        activity.setVerb(Verb.LISTEN);
        activity.setObject(new Song());
        activity.setContext(new Context(DateTime.now()));
        return new ResolvedActivity(
                UUID.randomUUID(),
                activity,
                null
        );
    }

    public class FilterTestModule extends AbstractModule {

        @Override
        protected void configure() {
            FilterManager filterManager = new InMemoryFilterManager();
            try {
                Set<String> queues = new HashSet<String>(1);
                queues.add("comment-queue");
                filterManager.register(
                        "rai-comment-filter",
                        "test rai comment filter",
                        queues,
                        getActivityCommentPattern()
                );
                filterManager.start("rai-comment-filter");
            } catch (FilterManagerException e) {
                throw new RuntimeException(e);
            }
            try {
                Set<String> queues = new HashSet<String>(1);
                queues.add("watch-queue");
                filterManager.register(
                        "watched-filter",
                        "watch comment filter",
                        queues,
                        getActivityObjectPattern()
                );
                filterManager.start("watched-filter");
            } catch (FilterManagerException e) {
                throw new RuntimeException(e);
            }
            bind(FilterManager.class).toInstance(filterManager);
            bind(FilterService.class).to(InMemoryFilterServiceImpl.class);
        }

        public ActivityPattern getActivityCommentPattern() {
            ActivityPattern ap = new ActivityPattern(
                    UUIDPattern.ANY,
                    VerbPattern.ANY,
                    new CommentPattern(new StringPattern("event-id"), URLPattern.ANY),
                    ContextPattern.ANY
            );
            return ap;
        }

        public ActivityPattern getActivityObjectPattern() {
            ActivityPattern ap = new ActivityPattern(
                    UUIDPattern.ANY,
                    new VerbPattern(Verb.WATCHED),
                    ObjectPattern.ANY,
                    ContextPattern.ANY
            );
            return ap;
        }

    }
}
