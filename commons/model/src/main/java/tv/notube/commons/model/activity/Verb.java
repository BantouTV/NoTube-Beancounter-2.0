package tv.notube.commons.model.activity;

import java.io.Serializable;

import tv.notube.commons.tests.annotations.Random;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public enum Verb implements Serializable {
    LOCATED,
    SAVE,
    FOLLOWING,
    JOIN,
    SHARE,
    MAKEFRIEND,
    TAG,
    RSVP,
    FAVORITED,
    LIKE,
    LISTEN,
    TWEET,
    WATCHED,
    CHECKIN;

    @Random(names = {})
    Verb() {}
}
