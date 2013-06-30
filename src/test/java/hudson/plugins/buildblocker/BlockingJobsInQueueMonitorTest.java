package hudson.plugins.buildblocker;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Queue;
import hudson.model.labels.LabelAtom;
import hudson.tasks.Shell;
import jenkins.model.Jenkins;
import org.jvnet.hudson.test.HudsonTestCase;

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

        FreeStyleProject executorUsing = this.createFreeStyleProject("executorUsingJob");
        executorUsing.setAssignedLabel(label);
        executorUsing.getBuildersList().add(new Shell("sleep 5"));
        System.out.println("starting executor using job...");
        Future<FreeStyleBuild> future = executorUsing.scheduleBuild2(0);

        System.out.println("waiting till its running...");
        // wait until job started
        while(! (Jenkins.getInstance().getComputers()[0].getExecutors().get(0).isBusy() ||
                 Jenkins.getInstance().getComputers()[0].getExecutors().get(1).isBusy())) {
            TimeUnit.SECONDS.sleep(1);
            System.out.println("... " + future.isDone());
        }
        System.out.println("runs...");

        String blockingJobInQueueName = "blockingJobInQueue";
        FreeStyleProject blockingJobInQueue = this.createFreeStyleProject(blockingJobInQueueName);
        blockingJobInQueue.setAssignedLabel(label);
        blockingJobInQueue.getBuildersList().add(new Shell("sleep 1"));
        System.out.println("starting blocking job to stay in queue... " + Jenkins.getInstance().getQueue().getItems().length);
        blockingJobInQueue.scheduleBuild2(0);

        System.out.println("wait till is in queue...");
        while(Jenkins.getInstance().getQueue().isEmpty()) {
            System.out.print(".");
        }
        System.out.println("blocking job in queue: "+ ((Queue.Item)Jenkins.getInstance().getQueue().getItems()[0]).task.getDisplayName());

        FreeStyleProject blockedJob = this.createFreeStyleProject("blockedJob");
        blockedJob.setAssignedLabel(label);
        blockedJob.getBuildersList().add(new Shell("sleep 5"));
        System.out.println("starting blocked job... " + Jenkins.getInstance().getQueue().getItems().length);
        blockedJob.scheduleBuild2(0);
        System.out.println("started... queue: " + Jenkins.getInstance().getQueue().getItems().length);

        BlockingJobsInQueueMonitor monitor = new BlockingJobsInQueueMonitor(blockingJobInQueueName);
        //SubTask blockingJob = monitor.getBlockingJob(blockedJob);
        //System.out.println("blocking job: " + blockingJob);
//        assertNotNull(blockingJob);

    }

    public void testGetBlockingJob() throws Exception {

    }
}
