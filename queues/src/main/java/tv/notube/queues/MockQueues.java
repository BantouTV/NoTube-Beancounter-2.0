package tv.notube.queues;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class MockQueues implements Queues {

    private Queue<String> queue = new PriorityQueue();

    @Override
    public void push(String json) throws QueuesException {
        queue.add(json);
    }

    @Override
    public void shutDown() throws QueuesException {}
}
