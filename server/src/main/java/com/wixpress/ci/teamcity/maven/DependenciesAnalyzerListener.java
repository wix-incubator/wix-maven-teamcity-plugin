package com.wixpress.ci.teamcity.maven;

import com.wixpress.ci.teamcity.maven.workspace.MavenModule;

/**
 * @author yoav
 * @since 2/21/12
 */
public interface DependenciesAnalyzerListener {
    void collectingDependencies(MavenModule projectModule);

    void collectedDependencies(MavenModule projectModule);
}
