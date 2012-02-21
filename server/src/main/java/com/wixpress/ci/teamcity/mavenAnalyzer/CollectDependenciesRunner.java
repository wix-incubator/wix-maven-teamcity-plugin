package com.wixpress.ci.teamcity.mavenAnalyzer;

import com.wixpress.ci.teamcity.dependenciesTab.CollectingMessagesListenerLogger;
import com.wixpress.ci.teamcity.domain.LogMessage;
import com.wixpress.ci.teamcity.domain.MModule;
import com.wixpress.ci.teamcity.maven.listeners.*;
import com.wixpress.ci.teamcity.maven.workspace.MavenWorkspaceReader;
import com.wixpress.ci.teamcity.maven.workspace.fs.BuildTypeWorkspaceFilesystem;
import jetbrains.buildServer.serverSide.SBuildType;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

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
    private TeamCityBuildMavenDependenciesAnalyzer teamCityBuildMavenDependenciesAnalyzer;


    public CollectDependenciesRunner(TeamCityBuildMavenDependenciesAnalyzer teamCityBuildMavenDependenciesAnalyzer, SBuildType buildType) {
        this.teamCityBuildMavenDependenciesAnalyzer = teamCityBuildMavenDependenciesAnalyzer;
        this.buildType = buildType;
    }

    public void run() {
        try {
            BuildTypeWorkspaceFilesystem workspaceFilesystem = new BuildTypeWorkspaceFilesystem(TeamCityBuildMavenDependenciesAnalyzer.getTempDir(), buildType);
            try {
                MavenWorkspaceReader workspaceReader =  teamCityBuildMavenDependenciesAnalyzer.getMavenBooter().newWorkspaceReader(workspaceFilesystem, new LoggingMavenWorkspaceListener(listenerLogger));
                MavenRepositorySystemSession session = teamCityBuildMavenDependenciesAnalyzer.getMavenBooter().newRepositorySystemSession(new LoggingTransferListener(listenerLogger), new LoggingRepositoryListener(listenerLogger));
                session.setWorkspaceReader(workspaceReader);

                MModule mModule = teamCityBuildMavenDependenciesAnalyzer.getMavenDependenciesAnalyzer()
                        .getModuleDependencies(workspaceReader.getRootModule(), session, new LoggingDependenciesAnalyzerListener(listenerLogger));
//                mModule.accept(new LoggingModuleVisitor(listenerLogger));
                teamCityBuildMavenDependenciesAnalyzer.save(mModule, buildType, listenerLogger);
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
                teamCityBuildMavenDependenciesAnalyzer.saveError(buildType, listenerLogger);
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
