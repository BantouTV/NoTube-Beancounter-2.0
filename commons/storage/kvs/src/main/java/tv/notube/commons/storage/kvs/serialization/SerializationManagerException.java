package tv.notube.commons.storage.kvs.serialization;

/**
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class SerializationManagerException extends Exception {

    public SerializationManagerException(String message) {
        super(message);
    }

    public SerializationManagerException(String message, Exception e) {
        super(message, e);
    }

}