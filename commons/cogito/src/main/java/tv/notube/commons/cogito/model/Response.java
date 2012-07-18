package tv.notube.commons.cogito.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class Response {

    private Collection<Entities> entities = new ArrayList<Entities>();

    private Collection<Relevants> relevants = new ArrayList<Relevants>();

    public Collection<Relevants> getRelevants() {
        return relevants;
    }

    public void setRelevants(Collection<Relevants> relevants) {
        this.relevants = relevants;
    }

    public Collection<Entities> getEntities() {
        return entities;
    }

    public void setEntities(Collection<Entities> entities) {
        this.entities = entities;
    }

    public void addEntities(Entities entities) {
        if (entities.getEntities().size() > 0) {
            this.entities.add(entities);
        }
    }

    public void addRelevants(Relevants relevants) {
        if(relevants.getType().compareTo("DOMAINS") == 0
                && relevants.getRelevants().size() > 0) {
            this.relevants.add(relevants);
        }
    }

    @Override
    public String toString() {
        return "Response{" +
                "entities=" + entities +
                ", relevants=" + relevants +
                '}';
    }
}