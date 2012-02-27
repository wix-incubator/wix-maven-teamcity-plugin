package com.wixpress.ci.teamcity.mavenAnalyzer;

import com.wixpress.ci.teamcity.dependenciesTab.CollectingMessagesListenerLogger;
import com.wixpress.ci.teamcity.domain.CollectProgress;
import com.wixpress.ci.teamcity.domain.LogMessage;
import com.wixpress.ci.teamcity.domain.MModule;
import com.wixpress.ci.teamcity.maven.listeners.*;
import com.wixpress.ci.teamcity.maven.workspace.MavenWorkspaceReader;
import com.wixpress.ci.teamcity.maven.workspace.fs.BuildTypeWorkspaceFilesystem;
import jetbrains.buildServer.serverSide.SBuildType;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.joda.time.DateTime;

import java.util.List;

/**
* @author yoav
* @since 2/19/12
*/
class CollectDependenciesRunner implements Runnable {

    private SBuildType buildType;
    private CollectingMessagesListenerLogger listenerLogger = new CollectingMessagesListenerLogger();
    private DateTime completedTime = null;
    private boolean completed = false;
    private boolean isError = false;
    private MavenBuildTypeDependenciesAnalyzer mavenBuildAnalyzer;


    public CollectDependenciesRunner(MavenBuildTypeDependenciesAnalyzer mavenBuildAnalyzer, SBuildType buildType) {
        this.mavenBuildAnalyzer = mavenBuildAnalyzer;
        this.buildType = buildType;
    }

    public void run() {
        try {
            listenerLogger.info(String.format("Starting collection of dependencies for %s:%s", buildType.getProjectName(), buildType.getName()));
            BuildTypeWorkspaceFilesystem workspaceFilesystem = new BuildTypeWorkspaceFilesystem(MavenBuildTypeDependenciesAnalyzer.getTempDir(), buildType);
            try {
                MavenWorkspaceReader workspaceReader =  mavenBuildAnalyzer.getMavenBooter().newWorkspaceReader(workspaceFilesystem, new LoggingMavenWorkspaceListener(listenerLogger));
                MavenRepositorySystemSession session = mavenBuildAnalyzer.getMavenBooter().newRepositorySystemSession(new LoggingTransferListener(listenerLogger), new LoggingRepositoryListener(listenerLogger));
                session.setWorkspaceReader(workspaceReader);

                MModule mModule = mavenBuildAnalyzer.getMavenDependenciesAnalyzer()
                        .getModuleDependencies(workspaceReader.getRootModule(), session, new LoggingDependenciesAnalyzerListener(listenerLogger));
//                mModule.accept(new LoggingModuleVisitor(listenerLogger));
                mavenBuildAnalyzer.save(mModule, buildType, listenerLogger);
                listenerLogger.completedCollectingDependencies(buildType);
            }
            finally {
                workspaceFilesystem.close();
                completedTime = new DateTime();
                completed = true;
            }
        } catch (Exception e) {
            isError = true;
            listenerLogger.failedCollectingDependencies(buildType, e);
            try {
                mavenBuildAnalyzer.saveError(buildType, listenerLogger);
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
    
    public SBuildType getBuildType() {
        return buildType;
    }

    public CollectProgress getProgress(Integer position, String id) {
        if (position == null) {
            return new CollectProgress(listenerLogger.getMessages(), completed, !isError, id);
        }
        else {
            List<LogMessage> newMessages = listenerLogger.getMessages(position);
            return new CollectProgress(newMessages, position+newMessages.size(), completed, !isError, id);
        }
    }
}
