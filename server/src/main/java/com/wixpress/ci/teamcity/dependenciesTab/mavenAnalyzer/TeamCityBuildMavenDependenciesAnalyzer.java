package com.wixpress.ci.teamcity.dependenciesTab.mavenAnalyzer;

import com.wixpress.ci.teamcity.dependenciesTab.CollectingMessagesListenerLogger;
import com.wixpress.ci.teamcity.domain.LogMessage;
import com.wixpress.ci.teamcity.domain.MModule;
import com.wixpress.ci.teamcity.maven.MavenBooter;
import com.wixpress.ci.teamcity.maven.MavenProjectDependenciesAnalyzer;
import com.wixpress.ci.teamcity.maven.listeners.*;
import com.wixpress.ci.teamcity.maven.workspace.MavenWorkspaceReader;
import com.wixpress.ci.teamcity.maven.workspace.fs.BuildTypeWorkspaceFilesystem;
import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.vcs.VcsException;
import jetbrains.buildServer.vcs.VcsRootInstance;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Analyses the maven dependencies of a single TeamCity build configuration.
 * @author yoav
 * @since 2/16/12
 */
public class TeamCityBuildMavenDependenciesAnalyzer {

    private static final String DEPENDENCIES_STORAGE = "com.wixpress.dependencies-storage";
    private static final String BUILD_DEPENDENCIES = "build-dependencies";

    private static final File tempDir = new File(System.getProperty( "java.io.tmpdir" ));
    private MavenProjectDependenciesAnalyzer mavenDependenciesAnalyzer;
    private MavenBooter mavenBooter;
    // todo extract to a executer class
    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private Map<String, CollectDependenciesRunner> runningCollections = new ConcurrentHashMap<String, CollectDependenciesRunner>();
    private ObjectMapper objectMapper;

    public TeamCityBuildMavenDependenciesAnalyzer(MavenBooter mavenBooter, ObjectMapper objectMapper, MavenProjectDependenciesAnalyzer mavenProjectDependenciesAnalyzer) {
        this.mavenBooter = mavenBooter;
        this.mavenDependenciesAnalyzer = mavenProjectDependenciesAnalyzer;
        this.objectMapper = objectMapper;
    }

    /**
     * get the most up to date build dependencies in store without refresh
     * @param buildType build type for which to get the dependencies
     * @return dependencies result
     */
    public DependenciesResult getBuildDependencies(SBuildType buildType) {
        return getBuildDependencies(buildType, false);
    }

    /**
     * get the most up to date build dependencies in store performing refresh if cvs revision is newer
     * @param buildType build type for which to get the dependencies
     * @return dependencies result
     */
    public DependenciesResult analyzeDependencies(SBuildType buildType) {
        return getBuildDependencies(buildType, true);
    }

    public DependenciesResult forceAnalyzeDependencies(SBuildType buildType) {
        if (runningCollections.containsKey(buildType.getBuildTypeId()) &&
                runningCollections.get(buildType.getBuildTypeId()).isCompleted())
            return new DependenciesResult(ResultType.runningAsync);
        else {
            collectDependencies(buildType);
            return new DependenciesResult(ResultType.runningAsync);
        }
    }
    
    private DependenciesResult getBuildDependencies(SBuildType buildType, boolean refreshIfNeeded) {
        if (runningCollections.containsKey(buildType.getBuildTypeId())) {
            if (runningCollections.get(buildType.getBuildTypeId()).isCompleted())
                return load(buildType, false);
            else
                return new DependenciesResult(ResultType.runningAsync);
        }
        else {
            DependenciesResult dependenciesResult = load(buildType, true);
            if ((refreshIfNeeded && (dependenciesResult.getResultType() == ResultType.needsRefresh)) ||
                    (dependenciesResult.getResultType() == ResultType.notRun) ||
                    (dependenciesResult.getResultType() == ResultType.exception)) {
                collectDependencies(buildType);
                return new DependenciesResult(ResultType.runningAsync);
            }
            else
                return dependenciesResult;
        }
    }

    private boolean hasNewerVcsRevision(Map<String, String> savedVcsRevisions, Map<String, String> currentVcsRevisions) throws VcsException {
        return savedVcsRevisions.equals(currentVcsRevisions);
    }

    public CollectProgress getProgress(String buildTypeId, Integer position) {
        CollectDependenciesRunner runner = runningCollections.get(buildTypeId);
        if (runner != null) {
            return runner.getProgress(position, buildTypeId);
        }
        return new CollectProgress(buildTypeId);
    }

    public void close() {
        executorService.shutdown();
    }

    private Map<String, String> getBuildVcsRevisions(SBuildType buildType) throws VcsException {
        Map<String, String> vcsToVersion = newHashMap();
        for (VcsRootInstance instance: buildType.getVcsRootInstances())
            vcsToVersion.put(instance.getName(), instance.getCurrentRevision());
        return vcsToVersion;
    }

    private void collectDependencies(SBuildType buildType) {
        purgeOldRuns();
        CollectDependenciesRunner runner = new CollectDependenciesRunner(buildType);
        executorService.execute(runner);
        runningCollections.put(buildType.getBuildTypeId(), runner);
    }

    private DependenciesResult load(SBuildType buildType, boolean checkIfRefreshNeeded) {
        CustomDataStorage customDataStorage = buildType.getCustomDataStorage(DEPENDENCIES_STORAGE);
        String serialized = customDataStorage.getValue(BUILD_DEPENDENCIES);
        if (serialized == null)
            return new DependenciesResult(ResultType.notRun);
        try {
            ModuleStorage moduleStorage = objectMapper.readValue(serialized, ModuleStorage.class);
            if (moduleStorage.isException)
                return new DependenciesResult(moduleStorage.getMessages());
            if (checkIfRefreshNeeded && hasNewerVcsRevision(moduleStorage.getVcsRevisions(), getBuildVcsRevisions(buildType)))
                return new DependenciesResult(ResultType.needsRefresh, moduleStorage.getModule());
            else
                return new DependenciesResult(moduleStorage.getModule());
        } catch (IOException e) {
            return new DependenciesResult(e);
        } catch (VcsException e) {
            return new DependenciesResult(e);
        }
    }

    private void save(MModule mModule, SBuildType buildType, CollectingMessagesListenerLogger listenerLogger) throws IOException, VcsException {
        ModuleStorage moduleStorage = new ModuleStorage(mModule, getBuildVcsRevisions(buildType));
        String serializedModule = objectMapper.writeValueAsString(moduleStorage);
        buildType.getCustomDataStorage(DEPENDENCIES_STORAGE).putValue(BUILD_DEPENDENCIES, serializedModule);
        listenerLogger.info("build dependencies saved");
    }

    private void saveError(SBuildType buildType, CollectingMessagesListenerLogger listenerLogger) throws IOException {
        ModuleStorage moduleStorage = new ModuleStorage(listenerLogger.getMessages());
        String serializedModule = objectMapper.writeValueAsString(moduleStorage);
        buildType.getCustomDataStorage(DEPENDENCIES_STORAGE).putValue(BUILD_DEPENDENCIES, serializedModule);
        listenerLogger.info("build dependencies saved");
    }

    private void purgeOldRuns() {
        DateTime nowMinus5 = new DateTime().minusMinutes(5);
        for (Map.Entry<String, CollectDependenciesRunner> runningCollection: runningCollections.entrySet()) {
            if (runningCollection.getValue().isCompleted() &&
                    runningCollection.getValue().getCompletedTime().isBefore(nowMinus5))
                runningCollections.remove(runningCollection.getKey());
        }
    }

    private class CollectDependenciesRunner implements Runnable {

        private SBuildType buildType;
        private CollectingMessagesListenerLogger listenerLogger = new CollectingMessagesListenerLogger();
        private DateTime completedTime = null;
        private String id = UUID.randomUUID().toString();
        private boolean completed = false;


        public CollectDependenciesRunner(SBuildType buildType) {
            this.buildType = buildType;
        }

        public void run() {
            try {
                BuildTypeWorkspaceFilesystem workspaceFilesystem = new BuildTypeWorkspaceFilesystem(tempDir, buildType);
                try {
                    MavenWorkspaceReader workspaceReader =  mavenBooter.newWorkspaceReader(workspaceFilesystem, new LoggingMavenWorkspaceListener(listenerLogger));
                    MavenRepositorySystemSession session = mavenBooter.newRepositorySystemSession(new LoggingTransferListener(listenerLogger), new LoggingRepositoryListener(listenerLogger));
                    session.setWorkspaceReader(workspaceReader);

                    MModule mModule = mavenDependenciesAnalyzer.getModuleDependencies(workspaceReader.getRootModule(), session);
                    mModule.accept(new LoggingModuleVisitor(listenerLogger));
                    save(mModule, buildType, listenerLogger);
                    listenerLogger.completedCollectingDependencies(buildType);
                }
                finally {
                    workspaceFilesystem.close();
                    completedTime = new DateTime();
                    completed = true;
                }
            } catch (Exception e) {
                listenerLogger.failedCollectingDependencies(buildType, e);
                try {
                    saveError(buildType, listenerLogger);
                } catch (Exception e1) {
                    // ignore this error
                }
            }
        }

        public boolean isCompleted() {
            return completed;
        }

        public DateTime getCompletedTime() {
            return completedTime;
        }

        public CollectProgress getProgress(Integer position, String id) {
            if (position == null) {
                return new CollectProgress(listenerLogger.getMessages(), completed, id);
            }
            else {
                List<LogMessage> newMessages = listenerLogger.getMessages(position);
                return new CollectProgress(newMessages, position+newMessages.size(), completed, id);
            }
        }
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
