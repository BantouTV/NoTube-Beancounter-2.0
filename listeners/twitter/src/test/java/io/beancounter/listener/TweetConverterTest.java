package io.beancounter.listener;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.beancounter.listener.model.TwitterTweet;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertTrue;

public class TweetConverterTest {

    private final TweetConverter underTest = new TweetConverter();
    private final Status status = mock(Status.class);
    private final User user = mock(User.class);
    private final URLEntity urlEntity = mock(URLEntity.class);
    private URLEntity[] urlEntities = new URLEntity[] {urlEntity};
    private final HashtagEntity hashtagEntity = mock(HashtagEntity.class);
    private final HashtagEntity[] hashtagEntities = new HashtagEntity[] {hashtagEntity};

    @BeforeMethod
    protected void setUp() throws Exception {
        when(status.getUser()).thenReturn(user);
        when(status.getURLEntities()).thenReturn(urlEntities);
        when(status.getHashtagEntities()).thenReturn(hashtagEntities);
    }

    @Test
    public void convertsCreatedDate() {
        Date createdDate = new Date();
        when(status.getCreatedAt()).thenReturn(createdDate);

        TwitterTweet twitterTweet = underTest.convert(status);
        assertThat(twitterTweet.getCreatedAt(), is(new DateTime(createdDate.getTime())));
    }

    @Test
    public void checkTwitterUserId() {
        final long userId = 2342352L;
        when(user.getId()).thenReturn(userId);

        TwitterTweet twitterTweet = underTest.convert(status);
        assertThat(twitterTweet.getUserId(), is(String.valueOf(userId)));
    }

    @Test
    public void convertsTweetUrl() {
        String userName = "Bob";
        when(user.getScreenName()).thenReturn(userName);

        TwitterTweet twitterTweet = underTest.convert(status);
        assertTrue(twitterTweet.getUrl().toExternalForm().contains(userName));
    }

    @Test
    public void convertsMentionedUrls() throws MalformedURLException {
        URL url = new URL("http://java.sun.com/index.html");
        when(urlEntity.getExpandedURL()).thenReturn(url);
        TwitterTweet twitterTweet = underTest.convert(status);
        assertThat(twitterTweet.getMentionedUrls().get(0), is(url));
    }

    @Test
    public void convertsHashTags() {
        String hashtag = "bean counter rocks!";
        when(hashtagEntity.getText()).thenReturn(hashtag);
        TwitterTweet twitterTweet = underTest.convert(status);
        assertThat(twitterTweet.getHashTags().iterator().next(), is(hashtag));
    }
}
