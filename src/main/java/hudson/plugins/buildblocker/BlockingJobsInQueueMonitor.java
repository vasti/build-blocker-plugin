package hudson.plugins.buildblocker;

import hudson.model.Queue;
import hudson.model.queue.SubTask;
import jenkins.model.Jenkins;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: fred
 * Date: 29.06.13
 * Time: 23:06
 * To change this template use File | Settings | File Templates.
 */
public class BlockingJobsInQueueMonitor extends BlockingJobsMonitorImpl implements BlockingJobsMonitor {
    /**
     * Constructor using the job configuration entry for blocking jobs
     *
     * @param blockingJobs line feed separated list og blocking jobs
     */
    public BlockingJobsInQueueMonitor(String blockingJobs) {
        super(blockingJobs);
    }

    /**
     * Returns the name of the first blocking job. If not found, it returns null.
     *
     * @param item The queue item for which we are checking whether it can run or not.
     *             or null if we are not checking a job from the queue (currently only used by testing).
     * @return the name of the first blocking job.
     */
    public SubTask getBlockingJob(Queue.Item item) {
        /**
         * check the list of items that have
         * already been approved for building
         * (but haven't actually started yet)
         */
        List<Queue.BuildableItem> buildableItems
                = Jenkins.getInstance().getQueue().getBuildableItems();

        for (Queue.BuildableItem buildableItem : buildableItems) {
            if(item != buildableItem) {
                for (String blockingJob : this.getBlockingJobs()) {
                    if(buildableItem.task.getFullDisplayName().matches(blockingJob)) {
                        return buildableItem.task;
                    }
                }
            }
        }

        return null;
    }
}
