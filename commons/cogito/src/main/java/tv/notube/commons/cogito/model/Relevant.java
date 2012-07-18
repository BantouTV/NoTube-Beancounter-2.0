package tv.notube.commons.cogito.model;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class Relevant {

    private String name;

    private double score;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Relevant{" +
                "name='" + name + '\'' +
                ", score='" + score + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Relevant relevant = (Relevant) o;

        if (Double.compare(relevant.score, score) != 0) return false;
        if (!name.equals(relevant.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}