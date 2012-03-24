package tv.notube.extension.profilingline;

import io.schema.engine.linker.LinkageRequest;
import io.schema.engine.linker.LinkerException;
import io.schema.engine.linker.LinkerResponse;
import io.schema.engine.linker.dbpedia.DBpediaLinkageRequest;
import io.schema.engine.linker.dbpedia.DBpediaLinker;
import io.schema.engine.linker.dbpedia.NoLinksFoundException;
import tv.notube.commons.model.activity.*;
import tv.notube.profiler.line.ProfilingLineItem;
import tv.notube.profiler.line.ProfilingLineItemException;

import java.lang.Object;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class FacebookProfilingLineItem extends ProfilingLineItem {

    private static final String FACEBOOK = "http://facebook.com";

    private DBpediaLinker linker;

    public FacebookProfilingLineItem(String name, String description) {
        super(name, description);
        linker = new DBpediaLinker("http://moth.notube.tv:8983/solr/");
    }

    @Override
    public void execute(Object o) throws ProfilingLineItemException {
        RawData intermediate = (RawData) o;
        if(intermediate.getActivities().size() == 0) {
            // just push the object down, there's nothing to profile here
            super.getNextProfilingLineItem().execute(intermediate);
            return;
        }
        List<Activity> activities = intermediate.getActivities();
        List<Activity> activitiesToBeRemoved = new ArrayList<Activity>();
        for (Activity activity : activities) {
            try {
                if (activity.getContext().getService().equals(new URL(FACEBOOK))
                        && activity.getVerb().equals(Verb.LIKE)) {
                    tv.notube.commons.model.activity.Object likeObj = activity.getObject();
                    String name = likeObj.getName();
                    String desc = likeObj.getDescription();
                    LinkageRequest linkageRequest = getLinkageRequest(name, desc);
                    LinkerResponse response;
                    try {
                        response = linker.link(linkageRequest);
                    } catch (NoLinksFoundException e) {
                        // just skip this activity
                        continue;
                    }
                    catch (LinkerException e) {
                        throw new ProfilingLineItemException(
                                "Error while processing activity '" +
                                        activity.toString() + "'",
                                e
                        );
                    }
                    URI link;
                    try {
                        link = response.getId().toURI();
                    } catch (URISyntaxException e) {
                        // just skip, it may happen
                        continue;
                    }
                    intermediate.addLinkedActivity(activity, link);
                    activitiesToBeRemoved.add(activity);
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException("URL '" + FACEBOOK + "' is not well formed", e);
            }
        }
        for(Activity activity : activitiesToBeRemoved) {
            intermediate.removeActivity(activity);
        }
        super.getNextProfilingLineItem().execute(intermediate);
    }

    private LinkageRequest getLinkageRequest(String name, String desc) {
        List<String> types = new ArrayList<String>();
        if(desc != null && !desc.equals("Unknown")) {
            String[] cats = desc.split("/");
            types = Arrays.asList(cats);
        }
        return new DBpediaLinkageRequest(
                name,
                types,
                true
        );
    }
}
