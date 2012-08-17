package io.beancounter.commons.model.activity;

import java.io.Serializable;

import io.beancounter.commons.tests.annotations.Random;

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
    SONG,
    TWEET,
    WATCHED,
    CHECKIN,
    COMMENT;
    @Random(names = {})
    Verb() {}
}
