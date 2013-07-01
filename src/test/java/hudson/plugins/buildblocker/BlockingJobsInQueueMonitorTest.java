/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Sun Microsystems, Inc., Frederik Fromm
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package hudson.plugins.buildblocker;

import hudson.model.Executor;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.labels.LabelAtom;
import hudson.model.queue.SubTask;
import hudson.tasks.Shell;
import jenkins.model.Jenkins;
import org.jvnet.hudson.test.HudsonTestCase;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests
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
        Future<FreeStyleBuild> f1 = executorUsing1.scheduleBuild2(0);
        FreeStyleProject executorUsing2 = this.createFreeStyleProject("executorUsingJob2");
        executorUsing2.setAssignedLabel(label);
        executorUsing2.getBuildersList().add(new Shell("sleep 5"));
        Future<FreeStyleBuild> f2 = executorUsing2.scheduleBuild2(0);

        // wait until job started

        List<Executor> executors = Jenkins.getInstance().getComputers()[0].getExecutors();
        while(! (executors.get(0).isBusy() && executors.get(1).isBusy())) {
            TimeUnit.SECONDS.sleep(1);
        }

        String blockingJobInQueueName = "blockingJobInQueue";
        FreeStyleProject blockingJobInQueue = this.createFreeStyleProject(blockingJobInQueueName);
        blockingJobInQueue.setAssignedLabel(label);
        blockingJobInQueue.getBuildersList().add(new Shell("sleep 1"));
        Future<FreeStyleBuild> f3 = blockingJobInQueue.scheduleBuild2(0);

        while(Jenkins.getInstance().getQueue().getBuildableItems().isEmpty()) {
            TimeUnit.MILLISECONDS.sleep(10);
        }

        FreeStyleProject blockedJob = this.createFreeStyleProject("blockedJob");
        blockedJob.setAssignedLabel(label);
        blockedJob.getBuildersList().add(new Shell("sleep 5"));
        Future<FreeStyleBuild> f4 = blockedJob.scheduleBuild2(0);

        BlockingJobsInQueueMonitor monitor = new BlockingJobsInQueueMonitor(blockingJobInQueueName);
        SubTask blockingJob = monitor.getBlockingJob(null);
        assertNotNull(blockingJob);

        // wait until blocking job stopped
        while(! (f1.isDone() && f2.isDone() && f3.isDone() && f4.isDone())) {
            TimeUnit.SECONDS.sleep(1);
        }

    }
}
