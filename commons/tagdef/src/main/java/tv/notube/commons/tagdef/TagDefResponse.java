package tv.notube.commons.tagdef;

import java.util.ArrayList;
import java.util.List;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class TagDefResponse {

    public enum Status {
        ERROR,
        OK
    }

    private List<Def> defs = new ArrayList<Def>();

    private Status status;

    private int amount;

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

}
