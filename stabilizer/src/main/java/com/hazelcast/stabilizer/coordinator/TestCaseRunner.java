package com.hazelcast.stabilizer.coordinator;

import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.stabilizer.TestCase;
import com.hazelcast.stabilizer.Utils;
import com.hazelcast.stabilizer.agent.workerjvm.WorkerJvmSettings;
import com.hazelcast.stabilizer.coordinator.remoting.AgentsClient;
import com.hazelcast.stabilizer.tests.Failure;
import com.hazelcast.stabilizer.tests.TestSuite;
import com.hazelcast.stabilizer.worker.commands.GenericCommand;
import com.hazelcast.stabilizer.worker.commands.InitCommand;
import com.hazelcast.stabilizer.worker.commands.RunCommand;
import com.hazelcast.stabilizer.worker.commands.StopCommand;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Set;

import static com.hazelcast.stabilizer.Utils.secondsToHuman;
import static java.lang.String.format;

/**
 * TestCase runner is responsible for running a single test case. Multiple test-cases can be run in parallel,
 * by having multiple TestCaseRunners  in parallel.
 */
public class TestCaseRunner {
    private final static ILogger log = Logger.getLogger(TestCaseRunner.class);

    private final TestCase testCase;
    private final Coordinator coordinator;
    private final AgentsClient agentsClient;
    private final TestSuite testSuite;
    private final NumberFormat performanceFormat = NumberFormat.getInstance(Locale.US);
    private final String prefix;
    private final Set<Failure.Type> nonCriticalFailures;

    public TestCaseRunner(TestCase testCase, TestSuite testSuite, Coordinator coordinator) {
        this.testCase = testCase;
        this.coordinator = coordinator;
        this.testSuite = testSuite;
        this.agentsClient = coordinator.agentsClient;
        this.prefix = testCase.id.equals("") ? "" : testCase.id + " ";

        nonCriticalFailures = testSuite.tolerableFailures;

    }

    public boolean run() throws Exception {
        log.info("--------------------------------------------------------------\n" +
                format("Running Test : %s\n%s", testCase.getId(), testCase) + "\n" +
                "--------------------------------------------------------------");

        int oldFailureCount = coordinator.failureList.size();
        try {
            echo(prefix + "Starting Test initialization");
            agentsClient.executeOnAllWorkers(new InitCommand(testCase));
            echo("Completed Test initialization");

            echo("Starting Test setup");
            agentsClient.executeOnAllWorkers(new GenericCommand(testCase.id, "setup"));
            agentsClient.waitDone(prefix, testCase.id);
            echo("Completed Test setup");

            echo("Starting Test local warmup");
            agentsClient.executeOnAllWorkers(new GenericCommand(testCase.id, "localWarmup"));
            agentsClient.waitDone(prefix, testCase.id);
            echo("Completed Test local warmup");

            echo("Starting Test global warmup");
            agentsClient.executeOnSingleWorker(new GenericCommand(testCase.id, "globalWarmup"));
            agentsClient.waitDone(prefix, testCase.id);
            echo("Completed Test global warmup");

            echo("Starting Test start");
            startTestCase();
            echo("Completed Test start");

            echo(format("Test will run for %s", secondsToHuman(testSuite.duration)));
            sleepSeconds(testSuite.duration);
            echo("Test finished running");

            echo("Starting Test stop");
            agentsClient.executeOnAllWorkers(new StopCommand(testCase.id));
            agentsClient.waitDone(prefix, testCase.id);
            echo("Completed Test stop");

            logPerformance();

            if (coordinator.verifyEnabled) {
                echo("Starting Test global verify");
                agentsClient.executeOnSingleWorker(new GenericCommand(testCase.id, "globalVerify"));
                agentsClient.waitDone(prefix, testCase.id);
                echo("Completed Test global verify");

                echo("Starting Test local verify");
                agentsClient.executeOnAllWorkers(new GenericCommand(testCase.id, "localVerify"));
                agentsClient.waitDone(prefix, testCase.id);
                echo("Completed Test local verify");
            } else {
                echo("Skipping Test verification");
            }

            echo("Starting Test global tear down");
            agentsClient.executeOnSingleWorker(new GenericCommand(testCase.id, "globalTeardown"));
            agentsClient.waitDone(prefix, testCase.id);
            echo("Finished Test global tear down");

            echo("Starting Test local tear down");
            agentsClient.waitDone(prefix, testCase.id);
            agentsClient.executeOnAllWorkers(new GenericCommand(testCase.id, "localTeardown"));
            echo("Completed Test local tear down");

            return coordinator.failureList.size() == oldFailureCount;
        } catch (Exception e) {
            log.severe("Failed", e);
            return false;
        }
    }

    private void logPerformance() {
        if (coordinator.monitorPerformance) {
            log.info("Operation-count: " + performanceFormat.format(coordinator.operationCount));
            double performance = (coordinator.operationCount * 1.0d) / testSuite.duration;
            log.info("Performance: " + performanceFormat.format(performance) + " ops/s");
        }
    }

    private void startTestCase() {
        WorkerJvmSettings workerJvmSettings = coordinator.workerJvmSettings;
        RunCommand runCommand = new RunCommand(testCase.id);
        runCommand.clientOnly = workerJvmSettings.mixedWorkerCount > 0 || workerJvmSettings.clientWorkerCount > 0;
        agentsClient.executeOnAllWorkers(runCommand);
    }

    public void sleepSeconds(int seconds) {
        int period = 30;
        int big = seconds / period;
        int small = seconds % period;

        for (int k = 1; k <= big; k++) {
            if (shouldTerminate()) {
                echo("Critical Failure detected, aborting execution of test");
                return;
            }

            Utils.sleepSeconds(period);
            final int elapsed = period * k;
            final float percentage = (100f * elapsed) / seconds;
            String msg = format("Running %s, %-4.2f percent complete", secondsToHuman(elapsed), percentage);

            if (coordinator.monitorPerformance) {
                msg += ", " + performanceFormat.format(coordinator.performance) + " ops/s.";
            }

            log.info(prefix + msg);
        }

        Utils.sleepSeconds(small);
    }

    private boolean shouldTerminate() {
        for (Failure failure : coordinator.failureList) {
            if (!nonCriticalFailures.contains(failure.type)) {
                return true;
            }
        }
        return false;
    }

    private void echo(String msg) {
        agentsClient.echo(prefix + msg);
        log.info(prefix + msg);
    }
}
