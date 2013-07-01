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
 * Unit tests
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
        Future<FreeStyleBuild> f1 = executorUsing.scheduleBuild2(0);

        String blockingJobName = "blockingJob";
        FreeStyleProject blockingJob = this.createFreeStyleProject(blockingJobName);
        blockingJob.setAssignedLabel(label);
        blockingJob.getBuildersList().add(new Shell("sleep 5"));
        Future<FreeStyleBuild> f2 = blockingJob.scheduleBuild2(0);

        // wait until job started

        List<Executor> executors = Jenkins.getInstance().getComputers()[0].getExecutors();
        while(! (executors.get(0).isBusy() && executors.get(1).isBusy())) {
            TimeUnit.SECONDS.sleep(1);
        }

        FreeStyleProject blockedJob = this.createFreeStyleProject("blockedJob");
        blockedJob.setAssignedLabel(label);
        blockedJob.getBuildersList().add(new Shell("sleep 5"));

        Queue queue = Jenkins.getInstance().getQueue();
        Future<FreeStyleBuild> f3 = blockedJob.scheduleBuild2(0);

        while(queue.getItems().length == 0) {
            TimeUnit.MILLISECONDS.sleep(100);
        }

        BlockingJobsInExecutionMonitor monitor = new BlockingJobsInExecutionMonitor(blockingJobName);
        SubTask blockingJobSubTask = monitor.getBlockingJob(null);
        assertNotNull(blockingJobSubTask);

        // wait until blocking job stopped
        while(! (f1.isDone() && f2.isDone() && f3.isDone())) {
            TimeUnit.SECONDS.sleep(1);
        }
    }
}
