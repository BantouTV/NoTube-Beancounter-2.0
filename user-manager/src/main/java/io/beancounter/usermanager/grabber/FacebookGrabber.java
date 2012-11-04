package io.beancounter.usermanager.grabber;

import com.google.common.collect.ImmutableMap;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.types.FacebookType;
import com.restfb.types.Post;
import io.beancounter.commons.model.User;
import io.beancounter.commons.model.activity.*;
import io.beancounter.commons.model.activity.Object;
import io.beancounter.commons.model.auth.Auth;
import io.beancounter.listener.facebook.core.FacebookUtils;
import io.beancounter.listener.facebook.core.converter.custom.Converter;
import io.beancounter.listener.facebook.core.converter.custom.ConverterException;
import io.beancounter.listener.facebook.core.converter.custom.FacebookLikeConverter;
import io.beancounter.listener.facebook.core.converter.custom.FacebookShareConverter;
import io.beancounter.listener.facebook.core.model.FacebookData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Alex Cowell
 */
public final class FacebookGrabber implements ActivityGrabber {

    private static final Logger LOG = LoggerFactory.getLogger(FacebookGrabber.class);

    public static final String SHARES = "shares";
    public static final String LIKES = "likes";

    private final User user;
    private final String serviceUserId;
    private final FacebookClient client;
    private final ImmutableMap<String, Integer> limits;

    public static FacebookGrabber create(User user, String serviceUserId) {
        Auth facebookAuth = user.getAuth("facebook");
        if (facebookAuth == null) {
            throw new IllegalArgumentException("User [" + user.getUsername() + "] does not have Facebook authentication");
        }

        // TODO: Load custom settings from bc.prop
        ImmutableMap<String, Integer> limits = ImmutableMap.of(
                SHARES, 10,
                LIKES, 5
        );
        DefaultFacebookClient facebookClient = new DefaultFacebookClient(facebookAuth.getSession());
        return new FacebookGrabber(user, serviceUserId, facebookClient, limits);
    }

    FacebookGrabber(User user, String serviceUserId, FacebookClient client, ImmutableMap<String, Integer> limits) {
        this.user = user;
        this.serviceUserId = serviceUserId;
        this.client = client;
        this.limits = ImmutableMap.copyOf(limits);
    }

    @Override
    public List<ResolvedActivity> grab() {
        List<ResolvedActivity> activities = new ArrayList<ResolvedActivity>();

        grabAndAddActivities(Post.class, Verb.SHARE, new FacebookShareConverter(), client, activities, "feed", SHARES);
        grabAndAddActivities(FacebookData.class, Verb.LIKE, new FacebookLikeConverter(), client, activities, "likes", LIKES);

        return activities;
    }

    private boolean hasValidLimit(String facebookType) {
        return limits.containsKey(facebookType) && limits.get(facebookType) > 0;
    }

    private <T extends FacebookType, M extends Object> void grabAndAddActivities(
            Class<T> type,
            Verb verb,
            Converter<T, M> converter,
            FacebookClient client,
            List<ResolvedActivity> activities,
            String field,
            String fieldType
    ) {
        if (!hasValidLimit(fieldType)) return;

        Collection<T> posts;
        try {
            posts = FacebookUtils.fetch(type, client, field, limits.get(fieldType));
        } catch (Exception ex) {
            // TODO (med): What is the desired behaviour here?
            LOG.error("Error grabbing activities from Facebook for user [{}]", serviceUserId, ex);
            return;
        }

        for (T post : posts) {
            Activity activity;
            try {
                Object object = converter.convert(post, true);
                Context context = converter.getContext(post, serviceUserId);
                activity = new Activity(verb, object, context);
            } catch (ConverterException cex) {
                LOG.warn("Could not convert Facebook {} from user [{}]", verb, serviceUserId);
                continue;
            }
            activities.add(new ResolvedActivity(user.getId(), activity, user));
        }
    }
}
