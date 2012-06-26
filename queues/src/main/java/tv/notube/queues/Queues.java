package tv.notube.queues;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface Queues {

    public void push(String json) throws QueuesException;

    public void shutDown() throws QueuesException;
}
