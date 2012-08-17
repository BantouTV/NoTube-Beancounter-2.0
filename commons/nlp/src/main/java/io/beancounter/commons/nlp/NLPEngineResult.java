package io.beancounter.commons.nlp;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class NLPEngineResult {

    private long executedAt = System.currentTimeMillis();

    private Set<Entity> entities = new HashSet<Entity>();

    private Set<Category> categories = new HashSet<Category>();

    public long getExecutedAt() {
        return executedAt;
    }

    public Set<Entity> getEntities() {
        return entities;
    }

    public void setEntities(Set<Entity> entities) {
        this.entities = entities;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public boolean addEntity(Entity entity) {
        return entities.add(entity);
    }

    public boolean addCategory(Category category) {
        return categories.add(category);
    }
}