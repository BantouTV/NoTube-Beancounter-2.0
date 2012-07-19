package tv.notube.commons.cogito.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class Entities {

    private String type;

    private Collection<Entity> entities = new ArrayList<Entity>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Collection<Entity> getEntities() {
        return entities;
    }

    public void setEntities(Collection<Entity> entities) {
        this.entities = entities;
    }

    public void addCogitoEntity(Entity entity) {
        if(entity.getSyncon() != null) {
            entities.add(entity);
        }
    }

    @Override
    public String toString() {
        return "Entities{" +
                "type='" + type + '\'' +
                ", entities=" + entities +
                '}';
    }
}