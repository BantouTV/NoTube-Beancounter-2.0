package tv.notube.extension.profilingline;

import io.schema.engine.linker.LinkageRequest;
import io.schema.engine.linker.LinkerException;
import io.schema.engine.linker.LinkerResponse;
import io.schema.engine.linker.dbpedia.DBpediaLinkageRequest;
import io.schema.engine.linker.dbpedia.DBpediaLinker;
import io.schema.engine.linker.dbpedia.NoLinksFoundException;
import org.sameas.sameas4j.SameAsService;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.Verb;
import tv.notube.commons.model.activity.bbc.BBCGenre;
import tv.notube.commons.model.activity.bbc.BBCProgramme;
import tv.notube.profiler.line.ProfilingLineItem;
import tv.notube.profiler.line.ProfilingLineItemException;

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
public class BBCProfilingLineItem extends ProfilingLineItem {

    private static final String IPLAYER = "http://www.bbc.co.uk/iplayer/";

    private SameAsService sameAsService;

    private DBpediaLinker linker;

    public BBCProfilingLineItem(String name, String description) {
        super(name, description);
        linker = new DBpediaLinker("http://moth.notube.tv:8983/solr/");
    }

    @Override
    public void execute(Object o) throws ProfilingLineItemException {
        RawData intermediate = (RawData) o;
        if (intermediate.getActivities().size() == 0) {
            // just push the object down, there's nothing to profile here
            super.getNextProfilingLineItem().execute(intermediate);
            return;
        }
        List<Activity> activities = intermediate.getActivities();
        List<Activity> activitiesToBeRemoved = new ArrayList<Activity>();
        for (Activity activity : activities) {
            try {
                if (activity.getContext().getService().equals(new URL(IPLAYER))
                        && activity.getVerb().equals(Verb.WATCHED)) {
                    BBCProgramme bbcPrg = (BBCProgramme) activity.getObject();
                    for (BBCGenre genre : bbcPrg.getGenres()) {
                        String gLabel = genre.getLabel();
                        LinkageRequest linkageRequest = getLinkageRequest(gLabel);
                        LinkerResponse response;
                        try {
                            response = linker.link(linkageRequest);
                        } catch (NoLinksFoundException e) {
                            // just skip this activity
                            continue;
                        } catch (LinkerException e) {
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
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException("URL '" + IPLAYER + "' is not well formed", e);
            }
        }
        for (Activity activity : activitiesToBeRemoved) {
            intermediate.removeActivity(activity);
        }
        super.getNextProfilingLineItem().execute(intermediate);
    }

    private LinkageRequest getLinkageRequest(String name) {
        List<String> types = new ArrayList<String>();
        return new DBpediaLinkageRequest(
                name,
                types,
                true
        );
    }
}
