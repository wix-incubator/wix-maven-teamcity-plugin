package com.wixpress.ci.teamcity.maven.listeners;

import com.wixpress.ci.teamcity.maven.DependenciesAnalyzerListener;
import com.wixpress.ci.teamcity.maven.workspace.MavenModule;

/**
 * @author yoav
 * @since 2/21/12
 */
public class LoggingDependenciesAnalyzerListener implements DependenciesAnalyzerListener {
    private ListenerLogger out;

    public LoggingDependenciesAnalyzerListener(ListenerLogger out)
    {
        this.out = out;
    }

    public void collectingDependencies(MavenModule projectModule) {
        out.info(String.format("collecting dependencies for [%s:%s:%s]", projectModule.getGroupId(), projectModule.getArtifactId(), projectModule.getVersion()));
    }

    public void collectedDependencies(MavenModule projectModule) {
        out.info(String.format("collected dependencies for [%s:%s:%s]", projectModule.getGroupId(), projectModule.getArtifactId(), projectModule.getVersion()));
    }
}
