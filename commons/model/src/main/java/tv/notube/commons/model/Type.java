package tv.notube.commons.model;

import java.util.HashSet;
import java.util.Set;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Type extends Referenceable {

    private Set<Interest> interests = new HashSet<Interest>();

    private double weight;

    public Type(Set<Interest> interests, double weight) {
        this.interests = interests;
        this.weight = weight;
    }

    public Set<Interest> getInterests() {
        return interests;
    }

    public void setInterests(Set<Interest> interests) {
        this.interests = interests;
    }

    public boolean addInterest(Interest interest) {
        return interests.add(interest);
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "Type{" +
                "interests=" + interests +
                "} " + super.toString();
    }

}
