package tv.notube.commons.model;

import java.io.Serializable;
import java.net.URI;
import java.util.UUID;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public abstract class Referenceable implements Serializable {

    private static final long serialVersionUID = 91420635L;

    protected UUID id;

    protected URI reference;

    public Referenceable() {
        id = UUID.randomUUID();
    }

    public Referenceable(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public URI getReference() {
        return reference;
    }

    public void setReference(URI reference) {
        this.reference = reference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Referenceable that = (Referenceable) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Referenceable{" +
                "id=" + id +
                ", reference=" + reference +
                '}';
    }
}
