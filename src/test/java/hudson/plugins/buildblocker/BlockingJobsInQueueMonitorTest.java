package hudson.plugins.buildblocker;

import hudson.model.Executor;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Queue;
import hudson.model.labels.LabelAtom;
import hudson.model.queue.SubTask;
import hudson.tasks.Shell;
import jenkins.model.Jenkins;
import org.jvnet.hudson.test.HudsonTestCase;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: fred
 * Date: 29.06.13
 * Time: 23:24
 * To change this template use File | Settings | File Templates.
 */
public class BlockingJobsInQueueMonitorTest extends HudsonTestCase {
    /**
     * One test for all for faster execution
     * @throws Exception
     */
    public void testConstructor() throws Exception {
        // clear queue from preceding tests
        Jenkins.getInstance().getQueue().clear();

        LabelAtom label = new LabelAtom("master");

        FreeStyleProject executorUsing1 = this.createFreeStyleProject("executorUsingJob1");
        executorUsing1.setAssignedLabel(label);
        executorUsing1.getBuildersList().add(new Shell("sleep 5"));
        System.out.println("starting executor using job 1 ...");
        Future<FreeStyleBuild> f1 = executorUsing1.scheduleBuild2(0);
        FreeStyleProject executorUsing2 = this.createFreeStyleProject("executorUsingJob2");
        executorUsing2.setAssignedLabel(label);
        executorUsing2.getBuildersList().add(new Shell("sleep 5"));
        System.out.println("starting executor using job 2 ...");
        Future<FreeStyleBuild> f2 = executorUsing2.scheduleBuild2(0);

        System.out.println("waiting till its running...");
        // wait until job started

        List<Executor> executors = Jenkins.getInstance().getComputers()[0].getExecutors();
        while(! (executors.get(0).isBusy() && executors.get(1).isBusy())) {
            TimeUnit.SECONDS.sleep(1);
            System.out.println("... " + f1.isDone() + " - " + executors.get(0).isBusy());
            System.out.println("... " + f2.isDone() + " - " + executors.get(1).isBusy());
        }
        System.out.println("runs...");

        String blockingJobInQueueName = "blockingJobInQueue";
        FreeStyleProject blockingJobInQueue = this.createFreeStyleProject(blockingJobInQueueName);
        blockingJobInQueue.setAssignedLabel(label);
        blockingJobInQueue.getBuildersList().add(new Shell("sleep 1"));
        System.out.println("starting blocking job to stay in queue... " + Jenkins.getInstance().getQueue().getBuildableItems().size());
        blockingJobInQueue.scheduleBuild2(0);

        System.out.println("wait till is in queue...");
        while(Jenkins.getInstance().getQueue().getBuildableItems().isEmpty()) {
            System.out.print(".");
        }
        System.out.println("blocking job in queue: "+ ((Queue.Item)Jenkins.getInstance().getQueue().getBuildableItems().get(0)).task.getDisplayName());

        FreeStyleProject blockedJob = this.createFreeStyleProject("blockedJob");
        blockedJob.setAssignedLabel(label);
        blockedJob.getBuildersList().add(new Shell("sleep 5"));
        System.out.println("starting blocked job... " + Jenkins.getInstance().getQueue().getBuildableItems().size());
        blockedJob.scheduleBuild2(0);
        System.out.println("started... queue: " + Jenkins.getInstance().getQueue().getBuildableItems().size());

        BlockingJobsInQueueMonitor monitor = new BlockingJobsInQueueMonitor(blockingJobInQueueName);
        System.out.println("monitoring... queue: " + Jenkins.getInstance().getQueue().getBuildableItems().size());
        SubTask blockingJob = monitor.getBlockingJob(null);
        System.out.println("done... queue: " + Jenkins.getInstance().getQueue().getBuildableItems().size());
        System.out.println("blocking job: " + blockingJob);
        assertNotNull(blockingJob);

    }
}
