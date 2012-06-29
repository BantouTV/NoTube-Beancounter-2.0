package tv.notube.activities;

/**
 * Raised if something goes wrong with {@link ActivityStore}.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class ActivityStoreException extends Exception {

    public ActivityStoreException(String message, Exception e) {
        super(message, e);
    }

}