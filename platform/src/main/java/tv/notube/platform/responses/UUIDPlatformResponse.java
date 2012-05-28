package tv.notube.platform.responses;

import tv.notube.platform.PlatformResponse;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.UUID;

/**
 *
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
@XmlRootElement
public class UUIDPlatformResponse extends PlatformResponse<UUID> {

    private UUID uuid;

    public UUIDPlatformResponse(Status status, String message, UUID uuid) {
        super(status, message);
        this.uuid = uuid;
    }

    @XmlElement
    public UUID getObject() {
        return uuid;
    }

}