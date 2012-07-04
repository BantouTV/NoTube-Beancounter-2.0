package tv.notube.crawler.requester.request.lastfm;

import tv.notube.commons.model.activity.Activity;
import tv.notube.commons.model.activity.ActivityBuilder;
import tv.notube.commons.model.activity.ActivityBuilderException;
import tv.notube.commons.model.activity.DefaultActivityBuilder;
import tv.notube.commons.model.activity.Song;
import tv.notube.commons.model.activity.Verb;
import tv.notube.crawler.requester.ServiceResponse;
import tv.notube.crawler.requester.ServiceResponseException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class LastFmTrackServiceResponse
        implements ServiceResponse<List<Activity>> {

    private final String lastfm = "http://last.fm";

    private List<LastFmTrack> tracks;

    private ActivityBuilder ab;

    public LastFmTrackServiceResponse(List<LastFmTrack> tracks) {
        this.tracks = tracks;
        ab = new DefaultActivityBuilder();
    }

    public List<Activity> getResponse() throws ServiceResponseException {
        List<Activity> activities = new ArrayList<Activity>();
        for (LastFmTrack track : tracks) {
            try {
                ab.push();
                ab.setVerb(Verb.LISTEN);
                Map<String, Object> fields = new HashMap<String, Object>();
                if (track.getArtist().getMbid() != null && !track.getArtist().getMbid().equals("")) {
                    fields.put("setMbid", track.getArtist().getMbid());
                }
                ab.setObject(Song.class, track.getUrl(), track.getName(), fields);
                // TODO (low) this is crap. understand where grab the lastfm
                // user identifier or username and push it correctly
                ab.setContext(track.getDate(), new URL(lastfm), "fake");
                activities.add(ab.pop());
            } catch (ActivityBuilderException e) {
                throw new ServiceResponseException("Error while building activity", e);
            } catch (MalformedURLException e) {
                throw new RuntimeException("URL '" + lastfm + "' is not well-formed", e);
            }
        }
        return activities;
    }
}
