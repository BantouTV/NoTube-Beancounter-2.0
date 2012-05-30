package tv.notube.commons.tests.randomisers;

import tv.notube.commons.tests.DefaultRandomiser;

import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class UUIDRandomiser extends DefaultRandomiser<UUID> {

    private int from;

    private int to;

    public UUIDRandomiser(String name) {
        super(name);
    }

    public Class<UUID> type() {
        return UUID.class;
    }

    public UUID getRandom() {
        return UUID.randomUUID();
    }

}
