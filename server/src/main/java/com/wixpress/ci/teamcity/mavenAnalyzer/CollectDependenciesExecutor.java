package com.wixpress.ci.teamcity.mavenAnalyzer;

import jetbrains.buildServer.serverSide.SBuildType;
import org.joda.time.DateTime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author yoav
 * @since 2/19/12
 */
public class CollectDependenciesExecutor {

    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private Map<String, CollectDependenciesRunner> runningCollections = new ConcurrentHashMap<String, CollectDependenciesRunner>();

    public CollectDependenciesRunner getRunner(SBuildType buildType) {
        return runningCollections.get(buildType.getBuildTypeId());
    }

    public CollectDependenciesRunner getRunner(String buildTypeId) {
        return runningCollections.get(buildTypeId);
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public void execute(CollectDependenciesRunner runner) {
        executorService.execute(runner);
        runningCollections.put(runner.getBuildType().getBuildTypeId(), runner);
    }

    public void purgeOldRuns() {
        DateTime nowMinus5 = new DateTime().minusMinutes(5);
        for (Map.Entry<String, CollectDependenciesRunner> runningCollection: runningCollections.entrySet()) {
            if (runningCollection.getValue().isCompleted() &&
                    runningCollection.getValue().getCompletedTime().isBefore(nowMinus5))
                runningCollections.remove(runningCollection.getKey());
        }
    }
}
