package tv.notube.listener.facebook.converter.custom;

import com.restfb.types.Post;
import org.joda.time.DateTime;
import tv.notube.commons.model.activity.*;
import tv.notube.commons.model.activity.Object;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This {@link tv.notube.listener.facebook.converter.custom.Converter} is
 * responsible of converting <i>Facebook</i> shares and likes to external sites
 * into {@link Object}s.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class FacebookShareConverter implements Converter<Post, tv.notube.commons.model.activity.Object> {

    private static final String SERVICE = "facebook";

    @Override
    public tv.notube.commons.model.activity.Object convert(Post post, boolean isOpenGraph) throws ConverterException {
        // shares, which are external URL, cannot be grabbed from opengraph
        return convert(post);
    }

    @Override
    public Context getContext(Post post) throws ConverterException {
        Context context = new Context();
        context.setDate(new DateTime(post.getCreatedTime()));
        context.setUsername(post.getId());
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
            throw new ConverterException("[" + candidateUrl + "] is ill-formed", e);
        }
        obj.setUrl(url);
        return obj;
    }

}
