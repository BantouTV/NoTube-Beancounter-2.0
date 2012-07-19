package tv.notube.filter.model.pattern;

import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class UUIDPattern implements Pattern<UUID> {

    public static final UUIDPattern ANY = new UUIDPattern();

    private UUID uuid;

    private UUIDPattern() {
        this.uuid = null;
    }

    public UUIDPattern(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean matches(UUID uuid) {
        return this.equals(ANY) || this.uuid.equals(uuid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UUIDPattern that = (UUIDPattern) o;

        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "UUIDPattern{" +
                "uuid=" + uuid +
                '}';
    }
}
