package tv.notube.extension.profilingline;

import tv.notube.commons.regexapi.*;
import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.Tweet;
import tv.notube.commons.tagdef.TagDef;
import tv.notube.commons.tagdef.TagDefException;
import tv.notube.profiler.line.ProfilingLineItem;
import tv.notube.profiler.line.ProfilingLineItemException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TwitterLinkerProfilingLineItem extends ProfilingLineItem {

    private static String TWITTER = "http://twitter.com";

    private RegexAPI regexAPI;

    public TwitterLinkerProfilingLineItem(
            String name,
            String description,
            Map<String, String> parameters
    ) {
        super(name, description, parameters);
        regexAPI = new RegexAPI(parameters.get("apikey"));
    }

    @Override
    public void execute(java.lang.Object o) throws ProfilingLineItemException {
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
                if (activity.getContext().getService().equals(new URL(TWITTER))) {
                    Tweet tweet = (Tweet) activity.getObject();
                    List<URI> resources;
                    // get resources from tweet text
                    try {
                        resources = getResources(tweet.getText());
                    } catch (RegexAPIException e) {
                        throw new ProfilingLineItemException("Error while " +
                                "calling RegexAPI", e);
                    }
                    // get resources from tweet eventual urls
                    for(URL url : tweet.getUrls()) {
                        resources.addAll(getResources(url));
                    }
                    // get resources from tweet eventual hashtags
                    for(String hashTag : tweet.getHashTags()) {
                        resources.addAll(getResourcesFromHashTag(hashTag));
                    }
                    intermediate.addLinkedActivity(activity, resources);
                    activitiesToBeRemoved.add(activity);
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException("URL '" + TWITTER + "' is not well formed", e);
            }
        }
        for(Activity activity : activitiesToBeRemoved) {
            intermediate.removeActivity(activity);
        }
        super.getNextProfilingLineItem().execute(intermediate);
    }

   private List<URI> getResourcesFromHashTag(String hashTag) throws ProfilingLineItemException {
        TagDef tagDef = new TagDef();
        List<String> defs;
        try {
            defs = tagDef.getDefinitions(hashTag);
        } catch (TagDefException e) {
            throw new ProfilingLineItemException(
                    "Error while accessing to TagDef for '" + hashTag + "'",
                    e
            );
        }
        List<URI> resources = new ArrayList<URI>();
        for (String def : defs) {
            RegexAPIResponse regexAPIResponse;
            try {
                regexAPIResponse = regexAPI.getNamedEntities(def);
            } catch (RegexAPIException e) {
                throw new ProfilingLineItemException("", e);
            }
            for (Identified identified : regexAPIResponse.getIdentified()) {
                NamedEntity namedEntity = (NamedEntity) identified;
                resources.add(namedEntity.getIdentifier());
            }
            try {
                regexAPIResponse = regexAPI.getRankedConcept(def);
            } catch (RegexAPIException e) {
                throw new ProfilingLineItemException("", e);
            }
            for (Identified identified : regexAPIResponse.getIdentified()) {
                Concept concept = (Concept) identified;
                resources.add(concept.getIdentifier());
            }
        }
        return resources;
    }

    private List<URI> getResources(URL url) throws ProfilingLineItemException {
        RegexAPIResponse regexAPIResponse;
        List<URI> resources = new ArrayList<URI>();
        try {
            regexAPIResponse = regexAPI.getNamedEntities(url);
        } catch (RegexAPIException e) {
            final String errMsg = "Error while calling the enricher";
            throw new ProfilingLineItemException(errMsg, e);
        }
        if (regexAPIResponse != null) {
            for (Identified identified : regexAPIResponse.getIdentified()) {
                NamedEntity namedEntity = (NamedEntity) identified;
                resources.add(namedEntity.getIdentifier());
            }
        }
        try {
            regexAPIResponse = regexAPI.getRankedConcept(url);
        } catch (RegexAPIException e) {
            throw new ProfilingLineItemException("", e);
        }
        if (regexAPIResponse != null) {
            for (Identified identified : regexAPIResponse.getIdentified()) {
                Concept concept = (Concept) identified;
                resources.add(concept.getIdentifier());
            }
        }
        return resources;
    }

    private List<URI> getResources(String text) throws RegexAPIException {
        List<URI> result = new ArrayList<URI>();
        RegexAPIResponse regexAPIResponse;
        regexAPIResponse = regexAPI.getNamedEntities(text);
        List<Identified> identifieds = regexAPIResponse.getIdentified();
        for (Identified identified : identifieds) {
            NamedEntity namedEntity = (NamedEntity) identified;
            result.add(namedEntity.getIdentifier());
        }
        regexAPIResponse = regexAPI.getRankedConcept(text);
        identifieds = regexAPIResponse.getIdentified();
        for (Identified identified : identifieds) {
            Concept concept = (Concept) identified;
            result.add(concept.getIdentifier());
        }
        return result;
    }


}
