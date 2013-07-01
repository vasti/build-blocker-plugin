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
public class BlockingJobsInExecutionMonitorTest extends HudsonTestCase {
    /**
     * One test for all for faster execution
     * @throws Exception
     */
    public void testConstructor() throws Exception {
        // clear queue from preceding tests
        Jenkins.getInstance().getQueue().clear();

        LabelAtom label = new LabelAtom("master");

        FreeStyleProject executorUsing = this.createFreeStyleProject("executorUsingJob1");
        executorUsing.setAssignedLabel(label);
        executorUsing.getBuildersList().add(new Shell("sleep 5"));
        System.out.println("starting executor using job 1 ...");
        Future<FreeStyleBuild> f1 = executorUsing.scheduleBuild2(0);

        String blockingJobName = "blockingJob";
        FreeStyleProject blockingJob = this.createFreeStyleProject(blockingJobName);
        blockingJob.setAssignedLabel(label);
        blockingJob.getBuildersList().add(new Shell("sleep 5"));
        System.out.println("starting executor using job 2 ...");
        Future<FreeStyleBuild> f2 = blockingJob.scheduleBuild2(0);

        System.out.println("waiting till its running...");
        // wait until job started

        List<Executor> executors = Jenkins.getInstance().getComputers()[0].getExecutors();
        while(! (executors.get(0).isBusy() && executors.get(1).isBusy())) {
            TimeUnit.SECONDS.sleep(1);
            System.out.println("... " + f1.isDone() + " - " + executors.get(0).isBusy());
            System.out.println("... " + f2.isDone() + " - " + executors.get(1).isBusy());
        }
        System.out.println("running ...");

        FreeStyleProject blockedJob = this.createFreeStyleProject("blockedJob");
        blockedJob.setAssignedLabel(label);
        blockedJob.getBuildersList().add(new Shell("sleep 5"));


        Queue queue = Jenkins.getInstance().getQueue();
        System.out.println("starting blocked job... " + queue.getItems().length);
        Future<FreeStyleBuild> f3 = blockedJob.scheduleBuild2(0);

        while(queue.getItems().length == 0) {
            TimeUnit.MILLISECONDS.sleep(100);
            System.out.print("." + queue.getItems().length);
        }
        System.out.println("started... queue: " + queue.getItems());


        BlockingJobsInExecutionMonitor monitor = new BlockingJobsInExecutionMonitor(blockingJobName);
        System.out.println("monitoring... queue: " + Jenkins.getInstance().getQueue().getBuildableItems().size());
        SubTask blockingJobSubTask = monitor.getBlockingJob(null);
        System.out.println("done... queue: " + Jenkins.getInstance().getQueue().getBuildableItems().size());
        System.out.println("blocking job: " + blockingJobSubTask);
        assertNotNull(blockingJobSubTask);

        System.out.println("f1: " + f1.isDone());
        System.out.println("f2: " + f2.isDone());
        System.out.println("f3: " + f3.isDone());

        System.out.println(! (f1.isDone() && f2.isDone() && f3.isDone()));

        // wait until blocking job stopped
        while(! (f1.isDone() && f2.isDone() && f3.isDone())) {
            System.out.println("f1: " + f1.isDone());
            System.out.println("f2: " + f2.isDone());
            System.out.println("f3: " + f3.isDone());

            TimeUnit.SECONDS.sleep(1);
        }

    }
}
