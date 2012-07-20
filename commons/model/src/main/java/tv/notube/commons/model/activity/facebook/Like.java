package tv.notube.commons.model.activity.facebook;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This class models something that could be liked on <i>Facebook</i>.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Like extends tv.notube.commons.model.activity.Object {

    private Collection<String> categories = new HashSet<String>();

    public Like() {
        super();
    }

    public Like(URL url) {
        super(url);
    }

    public Collection<String> getCategories() {
        return categories;
    }

    public void setCategories(Collection<String> categories) {
        this.categories = categories;
    }

    public boolean addCategory(String s) {
        return categories.add(s);
    }

    @Override
    public String toString() {
        return "Like{" +
                "categories=" + categories +
                '}';
    }
}
