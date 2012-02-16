package com.wixpress.ci.teamcity.dependenciesTab;

import com.wixpress.ci.teamcity.maven.MavenBooter;
import com.wixpress.ci.teamcity.maven.MavenProjectDependenciesAnalyzer;
import com.wixpress.ci.teamcity.maven.ModuleDependencies;
import com.wixpress.ci.teamcity.maven.listeners.*;
import com.wixpress.ci.teamcity.maven.workspace.MavenWorkspaceReader;
import com.wixpress.ci.teamcity.maven.workspace.MavenWorkspaceReaderException;
import com.wixpress.ci.teamcity.maven.workspace.fs.BuildTypeWorkspaceFilesystem;
import jetbrains.buildServer.serverSide.SBuildType;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.joda.time.DateTime;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.resolution.ArtifactDescriptorException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author yoav
 * @since 2/16/12
 */
public class TeamCityProjectDependenciesAnalyzer {

    private static final File tempDir = new File(System.getProperty( "java.io.tmpdir" ));
    private MavenProjectDependenciesAnalyzer mavenDependenciesAnalyzer;
    private MavenBooter mavenBooter;
    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private Map<String, CollectDependenciesRun> runningCollections = new ConcurrentHashMap<String, CollectDependenciesRun>();

    public TeamCityProjectDependenciesAnalyzer(MavenBooter mavenBooter) {
        this.mavenBooter = mavenBooter;
        this.mavenDependenciesAnalyzer = new MavenProjectDependenciesAnalyzer(mavenBooter.remoteRepositories(), mavenBooter.repositorySystem());
    }

    public String collectDependencies(SBuildType buildType) {
        purgeOldRuns();
        CollectDependenciesRun collectDependenciesRun = new CollectDependenciesRun(buildType);
        executorService.execute(collectDependenciesRun);
        runningCollections.put(collectDependenciesRun.getId(), collectDependenciesRun);
        return collectDependenciesRun.getId();
    }

    public void close() {
        executorService.shutdown();
    }

    private void purgeOldRuns() {
        DateTime nowMinus5 = new DateTime().minusMinutes(5);
        for (Map.Entry<String, CollectDependenciesRun> runningCollection: runningCollections.entrySet()) {
            if (runningCollection.getValue().isCompleted() &&
                    runningCollection.getValue().getCompletedTime().isBefore(nowMinus5))
                runningCollections.remove(runningCollection.getKey());
        }
    }

    public CollectProgress getProgress(String id, Integer position) {
        CollectDependenciesRun run = runningCollections.get(id);
        if (run != null) {
            return run.getProgress(position, id);
        }
        return new CollectProgress(id);
    }


    private class CollectDependenciesRun implements Runnable {

        private SBuildType buildType;
        private CollectingMessagesListenerLogger listenerLogger = new CollectingMessagesListenerLogger();
        private ModuleDependencies moduleDependencies;
        private DateTime completedTime = null;
        private String id = UUID.randomUUID().toString();
        private boolean completed = false;


        public CollectDependenciesRun(SBuildType buildType) {
            this.buildType = buildType;
        }

        public void run() {
            try {
                BuildTypeWorkspaceFilesystem workspaceFilesystem = new BuildTypeWorkspaceFilesystem(tempDir, buildType);
                try {
                    MavenWorkspaceReader workspaceReader =  mavenBooter.newWorkspaceReader(workspaceFilesystem, new LoggingMavenWorkspaceListener(listenerLogger));
                    MavenRepositorySystemSession session = mavenBooter.newRepositorySystemSession(new LoggingTransferListener(listenerLogger), new LoggingRepositoryListener(listenerLogger));
                    session.setWorkspaceReader(workspaceReader);

                    moduleDependencies = mavenDependenciesAnalyzer.getModuleDependencies(workspaceReader.getRootModule(), session);
                    moduleDependencies.accept(new LoggingModuleVisitor(listenerLogger));
                }
                finally {
                    workspaceFilesystem.close();
                    completedTime = new DateTime();
                    completed = true;
                }
            } catch (Exception e) {
                listenerLogger.error("Collecting Dependencies General Failure", e);
            }
        }

        public String getId() {
            return id;
        }

        public boolean isCompleted() {
            return completed;
        }

        public DateTime getCompletedTime() {
            return completedTime;
        }

        public ModuleDependencies getModuleDependencies() {
            return moduleDependencies;
        }

        public CollectProgress getProgress(Integer position, String id) {
            if (position == null) {
                return new CollectProgress(listenerLogger.getMessages(), completed, id);
            }
            else {
                List<CollectingMessagesListenerLogger.ListenerMessage> newMessages = listenerLogger.getMessages(position);
                return new CollectProgress(newMessages, position+newMessages.size(), completed, id);
            }
        }
    }
    
    public static class CollectProgress {
        private List<CollectingMessagesListenerLogger.ListenerMessage> messages;
        private int position;
        private boolean completed;
        private boolean runFound;
        private String id;

        public CollectProgress() {
        }

        public CollectProgress(List<CollectingMessagesListenerLogger.ListenerMessage> messages, boolean completed, String id) {
            this.messages = messages;
            this.position = messages.size();
            this.completed = completed;
            this.runFound = true;
            this.id = id;
        }

        public CollectProgress(List<CollectingMessagesListenerLogger.ListenerMessage> messages, int position, boolean completed, String id) {
            this.messages = messages;
            this.position = position;
            this.completed = completed;
            this.runFound = true;
            this.id = id;
        }

        public CollectProgress(String id) {
            this.messages = newArrayList();
            this.position = 0;
            this.completed = false;
            this.runFound = false;
            this.id = id;
        }

        public List<CollectingMessagesListenerLogger.ListenerMessage> getMessages() {
            return messages;
        }

        public void setMessages(List<CollectingMessagesListenerLogger.ListenerMessage> messages) {
            this.messages = messages;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        public boolean isRunFound() {
            return runFound;
        }

        public void setRunFound(boolean runFound) {
            this.runFound = runFound;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
