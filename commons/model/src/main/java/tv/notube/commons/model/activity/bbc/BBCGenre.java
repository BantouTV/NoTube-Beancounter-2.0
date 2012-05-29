package tv.notube.commons.model.activity.bbc;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class BBCGenre implements Serializable {

    private static final long serialVersionUID = 145679650135L;

    private URL reference;

    private String label;

    private List<BBCGenre> subGenres = new ArrayList<BBCGenre>();

    public BBCGenre() {}

    public BBCGenre(URL reference, String label) {
        this.reference = reference;
        this.label = label;
    }

    public URL getReference() {
        return reference;
    }

    public String getLabel() {
        return label;
    }

    public List<BBCGenre> getSubGenres() {
        return subGenres;
    }

    public boolean addSubgenre(BBCGenre bbcGenre) {
        return subGenres.add(bbcGenre);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BBCGenre bbcGenre = (BBCGenre) o;

        if (reference != null ? !reference.equals(bbcGenre.reference) : bbcGenre.reference != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return reference != null ? reference.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "BBCGenre{" +
                "reference=" + reference +
                ", label='" + label + '\'' +
                ", subGenres=" + subGenres +
                '}';
    }
}
