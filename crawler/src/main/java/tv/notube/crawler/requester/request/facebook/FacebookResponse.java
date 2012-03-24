package tv.notube.crawler.requester.request.facebook;

import tv.notube.commons.model.activity.*;
import tv.notube.commons.model.activity.Object;
import tv.notube.crawler.requester.ServiceResponse;
import tv.notube.crawler.requester.ServiceResponseException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FacebookResponse implements ServiceResponse<List<Activity>>{

    private List<FacebookLike> likes;

    public FacebookResponse(List<FacebookLike> likes) {
        this.likes = likes;
    }

    public List<Activity> getResponse() throws ServiceResponseException {
        List<Activity> activities = new ArrayList<Activity>();
        for(FacebookLike like : likes) {
            Activity activity;
            try {
                activity = getActivity(like);
            } catch (MalformedURLException e) {
                // just skip this activity
                continue;
            }
            activities.add(activity);
        }
        return activities;
    }

    private Activity getActivity(FacebookLike like) throws MalformedURLException {
        Activity activity = new Activity();
        activity.setVerb(Verb.LIKE);

        Object object = new Object();
        object.setName(like.getName());
        object.setDescription(like.getCategory());
        object.setUrl(new URL("http://facebook.com/" + like.getId()));
        activity.setObject(object);

        Context context = new Context();
        context.setService(new URL("http://facebook.com"));
        context.setDate(like.getCreatedAt());
        activity.setContext(context);

        return activity;
    }


}
