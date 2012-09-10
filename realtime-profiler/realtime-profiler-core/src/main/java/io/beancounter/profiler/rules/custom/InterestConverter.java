package io.beancounter.profiler.rules.custom;

import io.beancounter.commons.model.Interest;
import io.beancounter.commons.nlp.Category;
import io.beancounter.commons.nlp.Entity;
import io.beancounter.commons.nlp.NLPEngineResult;

import java.util.Collection;
import java.util.HashSet;

/**
 *
 * @author Enrico Candino ( enrico.candino @ gmail.com )
 */
public class InterestConverter {

    public static Collection<Interest> convert(NLPEngineResult result) {
        HashSet<Interest> interests = new HashSet<Interest>();
        double totScore = 0;
        for (Entity e : result.getEntities()) {
            Interest interest = new Interest(e.getLabel(), e.getResource());
            interest.setWeight(1);
            interests.add(interest);
            totScore++;
        }
        for (Category c : result.getCategories()) {
            Interest interest = new Interest(c.getLabel(), c.getResource());
            interest.setWeight(c.getScore());
            interests.add(interest);
            totScore += c.getScore();
        }
        return interests;
    }

    private static Collection<Interest> normalize(HashSet<Interest> interests, double totalScore) {
        for (Interest i : interests) {
            i.setWeight(i.getWeight() / totalScore);
        }
        return interests;
    }

}
