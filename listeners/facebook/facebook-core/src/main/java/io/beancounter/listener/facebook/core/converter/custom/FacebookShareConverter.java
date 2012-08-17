package io.beancounter.listener.facebook.core.converter.custom;

import com.restfb.types.Post;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.beancounter.commons.model.activity.*;
import io.beancounter.commons.model.activity.Object;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This {@link Converter} is
 * responsible of converting <i>Facebook</i> shares and likes to external sites
 * into {@link Object}s.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class FacebookShareConverter implements Converter<Post, io.beancounter.commons.model.activity.Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookShareConverter.class);

    private static final String SERVICE = "facebook";

    @Override
    public io.beancounter.commons.model.activity.Object convert(Post post, boolean isOpenGraph)
            throws ConverterException {
        // check if it has an external URL
        if(post.getLink() == null) {
            throw new UnconvertableException("facebook post with id [" + post.getId() + "] " +
                    "cannot be converted into a beancounter.io activity");
        }
        // shares, which are external URL, cannot be grabbed from opengraph
        return convert(post);
    }

    @Override
    public Context getContext(Post post, String userId) throws ConverterException {
        Context context = new Context();
        context.setDate(new DateTime(post.getCreatedTime()));
        context.setUsername(userId);
        context.setService(SERVICE);
        return context;
    }

    private Object convert(Post post) throws ConverterException {
        Object obj = new Object();
        obj.setName(post.getName());
        obj.setDescription(post.getDescription());

        String candidateUrl = post.getLink();
        URL url;
        try {
            url = new URL(candidateUrl);
        } catch (MalformedURLException e) {
            final String errMsg = "[" + candidateUrl + "] is ill-formed";
            LOGGER.error(errMsg, e);
            throw new ConverterException(errMsg, e);
        }
        obj.setUrl(url);
        return obj;
    }

}
