package com.wixpress.ci.teamcity.mavenAnalyzer;

import com.wixpress.ci.teamcity.dependenciesTab.CollectingMessagesListenerLogger;
import com.wixpress.ci.teamcity.domain.*;
import com.wixpress.ci.teamcity.maven.MavenBooter;
import com.wixpress.ci.teamcity.maven.MavenProjectDependenciesAnalyzer;
import com.wixpress.ci.teamcity.DependenciesAnalyzer;
import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.vcs.VcsException;
import jetbrains.buildServer.vcs.VcsRootInstance;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Analyses the maven dependencies of a single TeamCity build configuration.
 * @author yoav
 * @since 2/16/12
 */
public class TeamCityBuildMavenDependenciesAnalyzer implements DependenciesAnalyzer<MavenDependenciesResult> {

    public static final String DEPENDENCIES_STORAGE = "com.wixpress.dependencies-storage";
    public static final String BUILD_DEPENDENCIES = "build-dependencies";

    private static final File tempDir = new File(System.getProperty( "java.io.tmpdir" ));
    private MavenProjectDependenciesAnalyzer mavenDependenciesAnalyzer;
    private MavenBooter mavenBooter;
    private CollectDependenciesExecutor executor;
    private ObjectMapper objectMapper;

    public TeamCityBuildMavenDependenciesAnalyzer(MavenBooter mavenBooter, ObjectMapper objectMapper, MavenProjectDependenciesAnalyzer mavenProjectDependenciesAnalyzer, CollectDependenciesExecutor executor) {
        this.mavenBooter = mavenBooter;
        this.mavenDependenciesAnalyzer = mavenProjectDependenciesAnalyzer;
        this.objectMapper = objectMapper;
        this.executor = executor;
    }

    /**
     * get the most up to date build dependencies in store without refresh
     * @param buildType build type for which to get the dependencies
     * @return dependencies result
     */
    public MavenDependenciesResult getBuildDependencies(SBuildType buildType) {
        return getBuildDependencies(buildType, false);
    }

    /**
     * get the most up to date build dependencies in store performing refresh if cvs revision is newer
     * @param buildType build type for which to get the dependencies
     * @return dependencies result
     */
    public MavenDependenciesResult analyzeDependencies(SBuildType buildType) {
        return getBuildDependencies(buildType, true);
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
    
    private MavenDependenciesResult getBuildDependencies(SBuildType buildType, boolean refreshIfNeeded) {
        CollectDependenciesRunner runner = executor.getRunner(buildType);
        if (runner != null) {
            if (runner.isCompleted())
                return load(buildType, false);
            else
                return new MavenDependenciesResult(ResultType.runningAsync);
        }
        else {
            MavenDependenciesResult dependenciesResult = load(buildType, true);
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
        CustomDataStorage customDataStorage = buildType.getCustomDataStorage(DEPENDENCIES_STORAGE);
        String serialized = customDataStorage.getValue(BUILD_DEPENDENCIES);
        if (serialized == null)
            return new MavenDependenciesResult(ResultType.notRun);
        try {
            ModuleStorage moduleStorage = objectMapper.readValue(serialized, ModuleStorage.class);
            if (moduleStorage.isException)
                return new MavenDependenciesResult(moduleStorage.getMessages());
            if (checkIfRefreshNeeded && hasNewerVcsRevision(moduleStorage.getVcsRevisions(), getBuildVcsRevisions(buildType)))
                return new MavenDependenciesResult(ResultType.needsRefresh, moduleStorage.getModule());
            else
                return new MavenDependenciesResult(moduleStorage.getModule());
        } catch (IOException e) {
            return new MavenDependenciesResult(e);
        } catch (VcsException e) {
            return new MavenDependenciesResult(e);
        }
    }

    void save(MModule mModule, SBuildType buildType, CollectingMessagesListenerLogger listenerLogger) throws IOException, VcsException {
        ModuleStorage moduleStorage = new ModuleStorage(mModule, getBuildVcsRevisions(buildType));
        String serializedModule = objectMapper.writeValueAsString(moduleStorage);
        buildType.getCustomDataStorage(DEPENDENCIES_STORAGE).putValue(BUILD_DEPENDENCIES, serializedModule);
        listenerLogger.info("build dependencies saved");
    }

    void saveError(SBuildType buildType, CollectingMessagesListenerLogger listenerLogger) throws IOException {
        ModuleStorage moduleStorage = new ModuleStorage(listenerLogger.getMessages());
        String serializedModule = objectMapper.writeValueAsString(moduleStorage);
        buildType.getCustomDataStorage(DEPENDENCIES_STORAGE).putValue(BUILD_DEPENDENCIES, serializedModule);
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

    public static class ModuleStorage {
        private MModule module;
        private Map<String, String> vcsRevisions = newHashMap();
        private List<LogMessage> messages = newArrayList();
        private boolean isException;

        public ModuleStorage() {
        }

        public ModuleStorage(MModule module, Map<String, String> vcsRevisions) {
            this.module = module;
            this.vcsRevisions = vcsRevisions;
            this.isException = false;
        }

        public ModuleStorage(List<LogMessage> messages) {
            this.messages = messages;
            this.isException = true;
        }

        public MModule getModule() {
            return module;
        }

        public void setModule(MModule module) {
            this.module = module;
        }

        public Map<String, String> getVcsRevisions() {
            return vcsRevisions;
        }

        public void setVcsRevisions(Map<String, String> vcsRevisions) {
            this.vcsRevisions = vcsRevisions;
        }

        public List<LogMessage> getMessages() {
            return messages;
        }

        public void setMessages(List<LogMessage> messages) {
            this.messages = messages;
        }

        public boolean isException() {
            return isException;
        }

        public void setException(boolean exception) {
            isException = exception;
        }
    }
}
