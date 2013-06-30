package hudson.plugins.buildblocker;

import hudson.model.Queue;
import hudson.model.queue.SubTask;

/**
 * Created with IntelliJ IDEA.
 * User: fred
 * Date: 29.06.13
 * Time: 23:04
 * To change this template use File | Settings | File Templates.
 */
public interface BlockingJobsMonitor {
    /**
     * Returns the name of the first blocking job. If not found, it returns null.
     *
     * @param item The queue item for which we are checking whether it can run or not.
     *             or null if we are not checking a job from the queue (currently only used by testing).
     * @return the name of the first blocking job.
     */
    public SubTask getBlockingJob(Queue.Item item);
}
