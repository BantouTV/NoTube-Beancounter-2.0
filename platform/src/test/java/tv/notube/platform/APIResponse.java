package tv.notube.platform;

/**
 * This class, used only for test purposes, models the API Json responses
 * as a plain String object.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class APIResponse {

    private String object;

    private String message;

    private String status;

    public APIResponse() {}

    public APIResponse(String object, String message, String status) {
        this.object = object;
        this.message = message;
        this.status = status;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        APIResponse that = (APIResponse) o;

        if (message != null ? !message.equals(that.message) : that.message != null)
            return false;
        if (object != null ? !object.equals(that.object) : that.object != null)
            return false;
        if (status != null ? !status.equals(that.status) : that.status != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = object != null ? object.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }
}
