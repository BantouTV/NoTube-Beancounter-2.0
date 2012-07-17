package tv.notube.commons.model.activity;

import java.io.Serializable;

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
    COMMENT
}
