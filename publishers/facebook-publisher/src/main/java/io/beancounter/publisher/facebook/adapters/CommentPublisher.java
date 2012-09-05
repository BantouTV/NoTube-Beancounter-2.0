package io.beancounter.publisher.facebook.adapters;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;
import io.beancounter.commons.model.activity.Verb;
import io.beancounter.commons.model.activity.rai.Comment;

/**
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class CommentPublisher implements Publisher<Comment> {

    @Override
    public FacebookType publishActivity(String token, Verb verb, Comment comment) {
        FacebookClient client = new DefaultFacebookClient(token);
        return client.publish(
                "me/feed",
                FacebookType.class,
                Parameter.with("message", getMessage(comment)),
                Parameter.with("link", comment.getUrl().toString())
        );
    }

    private String getMessage(Comment comment) {
        String message = comment.getText() + "\n" + comment.getUrl().toString();
        return message;
    }
}
