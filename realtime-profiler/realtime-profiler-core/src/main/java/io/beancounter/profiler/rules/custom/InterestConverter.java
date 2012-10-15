package io.beancounter.profiler.rules.custom;

import io.beancounter.commons.model.Interest;
import io.beancounter.commons.model.Topic;
import io.beancounter.commons.nlp.Category;
import io.beancounter.commons.nlp.Entity;
import io.beancounter.commons.nlp.Feature;
import io.beancounter.commons.nlp.NLPEngineResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Enrico Candino ( enrico.candino @ gmail.com )
 */
public class InterestConverter {

    private static Collection<Interest> normalize(HashSet<Interest> interests, double totalScore) {
        for (Interest i : interests) {
            i.setWeight(i.getWeight() / totalScore);
        }
        return interests;
    }

    public static <T extends Feature> Collection<Interest> toInterests(Set<T> features) {
        HashSet<Interest> interests = new HashSet<Interest>();
        for (T e : features) {
            Entity entity = (Entity) e;
            Interest interest = new Interest(entity.getLabel(), entity.getResource());
            interest.setWeight(1);
            interests.add(interest);
        }
        return interests;
    }

    public static <T extends Feature> Collection<io.beancounter.commons.model.Category> toCategories(Set<T> features) {
        HashSet<io.beancounter.commons.model.Category> cats =
                new HashSet<io.beancounter.commons.model.Category>();
        for (T e : features) {
            Category category = (Category) e;
            io.beancounter.commons.model.Category cat =
                    new io.beancounter.commons.model.Category(category.getLabel(), category.getResource());
            cat.setWeight(1);
            cats.add(cat);
        }
        return cats;
    }

}
