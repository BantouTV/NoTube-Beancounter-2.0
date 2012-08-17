package io.beancounter.commons.tagdef;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import io.beancounter.commons.tagdef.handler.TagDefResponseDeserializer;

import java.util.ArrayList;
import java.util.List;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@JsonDeserialize(using = TagDefResponseDeserializer.class)
public class TagDefResponse {

    public enum Status {
        ERROR,
        OK
    }

    private List<Def> defs = new ArrayList<Def>();

    private Status status;

    @JsonProperty(value = "num_defs")
    private int amount;

    public  TagDefResponse() {}

    public TagDefResponse(Status status) {
        this.status = status;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public List<Def> getDefs() {
        return defs;
    }

    public void addDef(Def def) {
        this.defs.add(def);
    }

    public Status getStatus() {
        return this.status;
    }

}
