package io.beancounter.commons.nlp;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class Category extends Feature {

    public static Category build(URI baseuri, String id, String label) {
        try {
            return new Category(
                    new URI(baseuri + "/" + id),
                    label
            );
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("[" + baseuri + "/" + id + "] is not a well formed URI", e);
        }
    }

    public static Category build(String uri, String label) {
        try {
            return new Category(
                    new URI(uri),
                    label
            );
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("[" + uri + "] is not a well formed URI", e);
        }
    }

    private double score;

    public Category(URI resource, String label) {
        super(resource, label);
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Category{" +
                "score=" + score +
                "} " + super.toString();
    }
}