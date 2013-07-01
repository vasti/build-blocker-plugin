package hudson.plugins.buildblocker;

import hudson.matrix.MatrixConfiguration;
import hudson.model.Computer;
import hudson.model.Executor;
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
public class BlockingJobsInExecutionMonitor extends BlockingJobsMonitorImpl implements BlockingJobsMonitor {
    /**
     * Constructor using the job configuration entry for blocking jobs
     *
     * @param blockingJobs line feed separated list og blocking jobs
     */
    public BlockingJobsInExecutionMonitor(String blockingJobs) {
        super(blockingJobs);
    }

    /**
     * Constructor using the already splitted blocking jobs list.
     * @param blockingJobs
     */
    public BlockingJobsInExecutionMonitor(List<String> blockingJobs) {
        super("");
        this.setBlockingJobs(blockingJobs);
    }

    /**
     * Returns the name of the first blocking job. If not found, it returns null.
     *
     * @param item The queue item for which we are checking whether it can run or not.
     *             or null if we are not checking a job from the queue (currently only used by testing).
     * @return the name of the first blocking job.
     */
    public SubTask getBlockingJob(Queue.Item item) {
        Computer[] computers = Jenkins.getInstance().getComputers();

        for (Computer computer : computers) {
            List<Executor> executors = computer.getExecutors();

            executors.addAll(computer.getOneOffExecutors());

            for (Executor executor : executors) {
                if(executor.isBusy()) {
                    Queue.Executable currentExecutable = executor.getCurrentExecutable();

                    SubTask subTask = currentExecutable.getParent();
                    Queue.Task task = subTask.getOwnerTask();

                    if (task instanceof MatrixConfiguration) {
                        task = ((MatrixConfiguration) task).getParent();
                    }

                    for (String blockingJob : this.getBlockingJobs()) {
                        if(task.getFullDisplayName().matches(blockingJob)) {
                            return subTask;
                        }
                    }
                }
            }
        }

        return null;
    }
}
