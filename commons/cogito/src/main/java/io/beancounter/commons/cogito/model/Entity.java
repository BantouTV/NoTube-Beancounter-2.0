package io.beancounter.commons.cogito.model;

/**
 * @author Enrico Candino ( enrico.candino@gmail.com )
 */
public class Entity {

    private String name;

    private String syncon;

    private boolean isSyncon;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSyncon() {
        return syncon;
    }

    public synchronized void setSyncon(String syncon) {
        if(syncon.compareTo("SYNCON") == 0) {
            isSyncon = true;
        } else if(isSyncon) {
            this.syncon = syncon;
            isSyncon = false;
        }
    }

    @Override
    public String toString() {
        return "Entity{" +
                "name='" + name + '\'' +
                ", syncon='" + syncon + '\'' +
                '}';
    }
}