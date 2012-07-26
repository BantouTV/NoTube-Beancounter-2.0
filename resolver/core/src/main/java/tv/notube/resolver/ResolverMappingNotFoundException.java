package tv.notube.resolver;

/**
 * Raised if a mapping in the {@link Resolver} has not been found.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ResolverMappingNotFoundException extends ResolverException {

    public ResolverMappingNotFoundException(String message, Exception e) {
        super(message, e);
    }

    public ResolverMappingNotFoundException(String message) {
        super(message);
    }
}
