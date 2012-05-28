package tv.notube.platform.responses;

import com.google.gson.annotations.Expose;
import tv.notube.platform.PlatformResponse;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.UUID;

/**
 * Defines the result of a processing.
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
@Produces(MediaType.APPLICATION_JSON)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class PlatformResponseUUID implements PlatformResponse<UUID> {

    public enum Status {
        OK,
        NOK
    }

    @Expose
    private Status status;

    @Expose
    private String message;

    @Expose
    private UUID UUID;

    public PlatformResponseUUID(){}

    public PlatformResponseUUID(Status s, String m) {
        status = s;
        message = m;
    }

    public PlatformResponseUUID(Status s, String m, UUID id) {
        status = s;
        message = m;
        UUID = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getObject() {
        return UUID;
    }

    public void setObject(UUID id) {
        this.UUID = id;
    }

}