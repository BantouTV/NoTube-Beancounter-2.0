package tv.notube.crawler.requester.request.twitter;

import tv.notube.commons.model.activity.*;
import tv.notube.commons.model.activity.bbc.BBCGenre;
import tv.notube.commons.model.activity.bbc.BBCProgramme;
import tv.notube.crawler.requester.ServiceResponse;
import tv.notube.crawler.requester.ServiceResponseException;
import tv.notube.crawler.requester.request.twitter.bbc.BBC;
import tv.notube.crawler.requester.request.twitter.bbc.BBCException;

import java.lang.Object;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TwitterResponse implements ServiceResponse<List<Activity>> {

    private final String twitter = "http://twitter.com";

    private final String iplayer = "http://www.bbc.co.uk/iplayer/";

    private List<TwitterTweet> twitterTweets;

    private ActivityBuilder ab;

    private BBC bbc;

    public TwitterResponse(List<TwitterTweet> twitterTweets) {
        this.twitterTweets = twitterTweets;
        ab = new DefaultActivityBuilder();
        bbc = new BBC();
    }

    public List<Activity> getResponse() throws ServiceResponseException {
        List<Activity> activities = new ArrayList<Activity>();
        for (TwitterTweet twitterTweet : twitterTweets) {
            try {
                ab.push();
                ab.setVerb(Verb.TWEET);
                Map<String, Object> fields = new HashMap<String, Object>();
                fields.put("setText", twitterTweet.getText());
                ab.setObject(
                        Tweet.class,
                        twitterTweet.getUrl(),
                        twitterTweet.getName(),
                        fields
                );
                for (String hashTag : twitterTweet.getHashTags()) {
                    ab.objectSetField("addHashTag", hashTag, String.class);
                }
                for (URL url : twitterTweet.getMentionedUrls()) {
                    ab.objectSetField("addUrl", url, URL.class);
                }
                ab.setContext(
                        twitterTweet.getCreatedAt(),
                        new URL(twitter),
                        twitterTweet.getUsername()
                );
                activities.add(ab.pop());
            } catch (ActivityBuilderException e) {
                throw new ServiceResponseException("Error while building activity", e);
            } catch (MalformedURLException e) {
                throw new RuntimeException("URL '" + twitter + "' is not " +
                        "well-formed", e);
            }
            if (containsBBCUrl(twitterTweet.getMentionedUrls())) {
                List<BBCProgramme> programmes = getBBCPrograms(
                        twitterTweet.getMentionedUrls()
                );
                for (BBCProgramme bbcProgramme : programmes) {
                    try {
                        ab.push();
                        ab.setVerb(Verb.WATCHED);
                        Map<String, Object> fields = new HashMap<String, Object>();
                        fields.put("setPicture", bbcProgramme.getPicture());
                        fields.put("setMediumSynopsis", bbcProgramme.getMediumSynopsis());
                        fields.put("setDescription", bbcProgramme.getDescription());
                        ab.setObject(
                                BBCProgramme.class,
                                bbcProgramme.getUrl(),
                                bbcProgramme.getName(),
                                fields
                        );
                        for (BBCGenre genre : bbcProgramme.getGenres()) {
                            ab.objectSetField("addGenre", genre, BBCGenre.class);
                        }
                        for (String genre : bbcProgramme.getActors()) {
                            ab.objectSetField("addActor", genre, String.class);
                        }
                        ab.setContext(twitterTweet.getCreatedAt(), new URL(iplayer), twitterTweet.getUsername());
                        activities.add(ab.pop());
                    } catch (ActivityBuilderException e) {
                        throw new ServiceResponseException("Error while building activity", e);
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(
                                "URL '" + twitter + "' is not well-formed", e);
                    }
                }
            }
        }
        return activities;
    }

    private boolean containsBBCUrl(List<URL> urls) {
        for(URL url : urls) {
            if(bbc.isBBC(url)) {
                return true;
            }
        }
        return false;
    }

    private List<BBCProgramme> getBBCPrograms(List<URL> urls) {
        List<BBCProgramme> programmes = new ArrayList<BBCProgramme>();
        for (URL url : urls) {
            if (bbc.isBBC(url)) {
                String programmeId;
                try {
                    programmeId = bbc.getProgrammeId(url);
                } catch (BBCException e) {
                    continue;
                }
                URL programmeUrl;
                try {
                    programmeUrl = new URL(
                            "http://www.bbc.co.uk/programmes/" + programmeId + ".rdf");
                } catch (MalformedURLException e) {
                    // just skip
                    continue;
                }
                BBCProgramme bbcProgramme;
                try {
                    bbcProgramme = bbc.getProgramme(programmeUrl);
                } catch (BBCException e) {
                    continue;
                }
                programmes.add(bbcProgramme);
            }
        }
        return programmes;
    }

}