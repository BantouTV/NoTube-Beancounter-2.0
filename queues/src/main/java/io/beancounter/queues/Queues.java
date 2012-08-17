package io.beancounter.queues;

/**
 * This is the minimum contract a class that wants to interact with
 * queues must respect.
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public interface Queues {

    /**
     * Pushes a json object down the queue.
     *
     * @param json
     * @throws QueuesException
     */
    public void push(String json) throws QueuesException;

    /**
     * Shut down the connection.
     *
     * @throws QueuesException
     */
    public void shutDown() throws QueuesException;
}
