package com.wixpress.ci.teamcity.mavenAnalyzer;

import com.wixpress.ci.teamcity.dependenciesTab.CollectingMessagesListenerLogger;
import com.wixpress.ci.teamcity.domain.*;
import com.wixpress.ci.teamcity.maven.MavenBooter;
import com.wixpress.ci.teamcity.maven.MavenProjectDependenciesAnalyzer;
import com.wixpress.ci.teamcity.DependenciesAnalyzer;
import com.wixpress.ci.teamcity.mavenAnalyzer.dao.BuildTypeDependenciesStorage;
import com.wixpress.ci.teamcity.mavenAnalyzer.dao.DependenciesDao;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.vcs.VcsException;
import jetbrains.buildServer.vcs.VcsRootInstance;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Analyses the maven dependencies of a single TeamCity build configuration.
 * @author yoav
 * @since 2/16/12
 */
public class TeamCityBuildMavenDependenciesAnalyzer implements DependenciesAnalyzer<MavenDependenciesResult> {

    private static final File tempDir = new File(System.getProperty( "java.io.tmpdir" ));
    private MavenProjectDependenciesAnalyzer mavenDependenciesAnalyzer;
    private MavenBooter mavenBooter;
    private CollectDependenciesExecutor executor;
    private DependenciesDao dependenciesDao;

    public TeamCityBuildMavenDependenciesAnalyzer(MavenBooter mavenBooter, DependenciesDao dependenciesDao, MavenProjectDependenciesAnalyzer mavenProjectDependenciesAnalyzer, CollectDependenciesExecutor executor) {
        this.mavenBooter = mavenBooter;
        this.mavenDependenciesAnalyzer = mavenProjectDependenciesAnalyzer;
        this.dependenciesDao = dependenciesDao;
        this.executor = executor;
    }

    public MavenDependenciesResult getBuildDependencies(SBuildType buildType) {
        return getBuildDependencies(buildType, true);
    }

    /**
     * get the most up to date build dependencies in store without refresh
     * @param buildType build type for which to get the dependencies
     * @param checkForNewerRevision should we check if a found dependencies may need refresh cause of a new VCS revision?
     * @return dependencies result
     */
    public MavenDependenciesResult getBuildDependencies(SBuildType buildType, boolean checkForNewerRevision) {
        return getBuildDependencies(buildType, false, checkForNewerRevision);
    }

    /**
     * get the most up to date build dependencies in store performing refresh if cvs revision is newer
     * @param buildType build type for which to get the dependencies
     * @return dependencies result
     */
    public MavenDependenciesResult analyzeDependencies(SBuildType buildType) {
        return getBuildDependencies(buildType, true, true);
    }

    public MavenDependenciesResult forceAnalyzeDependencies(SBuildType buildType) {
        CollectDependenciesRunner runner = executor.getRunner(buildType);
        if (runner != null && !runner.isCompleted())
            return new MavenDependenciesResult(ResultType.runningAsync);
        else {
            collectDependencies(buildType);
            return new MavenDependenciesResult(ResultType.runningAsync);
        }
    }
    
    private MavenDependenciesResult getBuildDependencies(SBuildType buildType, boolean refreshIfNeeded, boolean checkForNewerRevision) {
        CollectDependenciesRunner runner = executor.getRunner(buildType);
        if (runner != null) {
            if (runner.isCompleted())
                return load(buildType, false);
            else
                return new MavenDependenciesResult(ResultType.runningAsync);
        }
        else {
            MavenDependenciesResult dependenciesResult = load(buildType, checkForNewerRevision);
            if ((refreshIfNeeded && (dependenciesResult.getResultType() == ResultType.needsRefresh)) ||
                    (dependenciesResult.getResultType() == ResultType.notRun)) {
                collectDependencies(buildType);
                return new MavenDependenciesResult(ResultType.runningAsync);
            }
            else
                return dependenciesResult;
        }
    }

    private boolean hasNewerVcsRevision(Map<String, String> savedVcsRevisions, Map<String, String> currentVcsRevisions) throws VcsException {
        return !savedVcsRevisions.equals(currentVcsRevisions);
    }

    public CollectProgress getProgress(String buildTypeId, Integer position) {
        CollectDependenciesRunner runner = executor.getRunner(buildTypeId);
        if (runner != null) {
            return runner.getProgress(position, buildTypeId);
        }
        return new CollectProgress(buildTypeId);
    }

    private Map<String, String> getBuildVcsRevisions(SBuildType buildType) throws VcsException {
        Map<String, String> vcsToVersion = newHashMap();
        for (VcsRootInstance instance: buildType.getVcsRootInstances())
            vcsToVersion.put(instance.getName(), instance.getCurrentRevision());
        return vcsToVersion;
    }

    private void collectDependencies(SBuildType buildType) {
        purgeOldRuns();
        CollectDependenciesRunner runner = new CollectDependenciesRunner(this, buildType);
        executor.execute(runner);
    }

    private MavenDependenciesResult load(SBuildType buildType, boolean checkIfRefreshNeeded) {
        try {
            BuildTypeDependenciesStorage dependenciesStorage = dependenciesDao.load(buildType);
            if (dependenciesStorage == null)
                return new MavenDependenciesResult(ResultType.notRun);
            else if (dependenciesStorage.isException())
                return new MavenDependenciesResult(dependenciesStorage.getMessages());
            else if (checkIfRefreshNeeded && hasNewerVcsRevision(dependenciesStorage.getVcsRevisions(), getBuildVcsRevisions(buildType)))
                return new MavenDependenciesResult(ResultType.needsRefresh, dependenciesStorage.getModule());
            else
                return new MavenDependenciesResult(dependenciesStorage.getModule());
        } catch (IOException e) {
            return new MavenDependenciesResult(e);
        } catch (VcsException e) {
            return new MavenDependenciesResult(e);
        }
    }

    void save(MModule mModule, SBuildType buildType, CollectingMessagesListenerLogger listenerLogger) throws IOException, VcsException {
        BuildTypeDependenciesStorage dependenciesStorage = new BuildTypeDependenciesStorage(mModule, getBuildVcsRevisions(buildType));
        dependenciesDao.save(dependenciesStorage, buildType);
        listenerLogger.info("build dependencies saved");
    }

    void saveError(SBuildType buildType, CollectingMessagesListenerLogger listenerLogger) throws IOException {
        BuildTypeDependenciesStorage dependenciesStorage = new BuildTypeDependenciesStorage(listenerLogger.getMessages());
        dependenciesDao.save(dependenciesStorage, buildType);
        listenerLogger.info("build dependencies saved");
    }

    private void purgeOldRuns() {
        executor.purgeOldRuns();
    }

    static File getTempDir() {
        return tempDir;
    }

    MavenProjectDependenciesAnalyzer getMavenDependenciesAnalyzer() {
        return mavenDependenciesAnalyzer;
    }

    MavenBooter getMavenBooter() {
        return mavenBooter;
    }

}
