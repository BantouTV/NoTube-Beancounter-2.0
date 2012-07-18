package tv.notube.commons.model.activity;

import java.io.Serializable;

import tv.notube.commons.tests.annotations.Random;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public enum Verb implements Serializable {
    LOCATED,
    @Deprecated
    FOLLOWING,
    SHARE,
    @Deprecated
    MAKEFRIEND,
    RSVP,
    FAVORITED,
    LIKE,
    LISTEN,
    TWEET,
    WATCHED,
    CHECKIN,
    COMMENT;
    @Random(names = {})
    Verb() {}
}
