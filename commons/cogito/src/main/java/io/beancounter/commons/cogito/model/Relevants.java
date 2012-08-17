package io.beancounter.commons.cogito.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class Relevants {

    private String type;

    private Collection<Relevant> relevants = new ArrayList<Relevant>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Collection<Relevant> getRelevants() {
        return relevants;
    }

    public void setRelevants(Collection<Relevant> relevants) {
        this.relevants = relevants;
    }

    public void addCogitoRelevant(Relevant relevant) {
        this.relevants.add(relevant);
    }

    @Override
    public String toString() {
        return "Relevants{" +
                "type='" + type + '\'' +
                ", relevants=" + relevants +
                '}';
    }
}