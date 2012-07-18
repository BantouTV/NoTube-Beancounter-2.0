package tv.notube.commons.nlp;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class Entity extends Feature {

    public static Entity build(URI baseuri, String id, String label) {
        try {
            return new Entity(
                    new URI(baseuri + "/" + id),
                    label
            );
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("[" + baseuri + "/" + id + "] is not a well formed URI", e);
        }
    }

    public static Entity build(String uri, String label) {
        try {
            return new Entity(
                    new URI(uri),
                    label
            );
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("[" + uri + "] is not a well formed URI", e);
        }
    }

    public Entity(URI resource, String label) {
        super(resource, label);
    }

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "type='" + type + '\'' +
                "} " + super.toString();
    }
}