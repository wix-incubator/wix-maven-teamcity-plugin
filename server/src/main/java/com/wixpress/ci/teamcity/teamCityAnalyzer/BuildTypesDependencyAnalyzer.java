package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.wixpress.ci.teamcity.DependenciesAnalyzer;
import com.wixpress.ci.teamcity.domain.*;
import com.wixpress.ci.teamcity.mavenAnalyzer.MavenBuildTypeDependenciesAnalyzer;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

/**
 * analyzes the dependencies between TeamCity build configurations based on Maven dependencies
 * @author yoav
 * @since 2/22/12
 */
public class BuildTypesDependencyAnalyzer implements DependenciesAnalyzer<BuildDependenciesResult>{

    private MavenBuildTypeDependenciesAnalyzer mavenBuildAnalyzer;
    private ProjectManager projectManager;
    private BuildTypeDependenciesDecorator dependenciesDecorator;

    public BuildTypesDependencyAnalyzer(MavenBuildTypeDependenciesAnalyzer mavenBuildAnalyzer, ProjectManager projectManager, BuildTypeDependenciesDecorator dependenciesDecorator) {
        this.mavenBuildAnalyzer = mavenBuildAnalyzer;
        this.projectManager = projectManager;
        this.dependenciesDecorator = dependenciesDecorator;
    }

    public BuildDependenciesResult getBuildDependencies(SBuildType buildType) {
        return getBuildDependencies(buildType, true);
    }

    /**
     * gets the BuildType dependencies as well as the maven dependencies of a project
     * @param buildType
     * @return
     */
    public BuildDependenciesResult getBuildDependencies(SBuildType buildType, boolean checkForNewerRevision) {
        MavenDependenciesResult mavenResult = mavenBuildAnalyzer.getBuildDependencies(buildType, checkForNewerRevision);
        if (mavenResult.getResultType().hasDependencies())
            return dependenciesDecorator.decorateWithBuildTypesAnalysis(mavenResult, buildType);
        else
            return toBuildDependenciesResult(mavenResult, buildType);
    }

    public BuildDependenciesResult analyzeDependencies(SBuildType buildType) {
        MavenDependenciesResult mavenResult = mavenBuildAnalyzer.analyzeDependencies(buildType);
        if (mavenResult.getResultType().hasDependencies())
            return dependenciesDecorator.decorateWithBuildTypesAnalysis(mavenResult, buildType);
        else
            return toBuildDependenciesResult(mavenResult, buildType);
    }

    public BuildDependenciesResult forceAnalyzeDependencies(SBuildType buildType) {
        MavenDependenciesResult mavenResult = mavenBuildAnalyzer.forceAnalyzeDependencies(buildType);
        return toBuildDependenciesResult(mavenResult, buildType);
    }

    public CollectProgress getProgress(String buildTypeId, Integer position) {
        return mavenBuildAnalyzer.getProgress(buildTypeId, position);
    }


    private BuildDependenciesResult toBuildDependenciesResult(MavenDependenciesResult mavenResult, SBuildType buildType) {
        return new BuildDependenciesResult(mavenResult, buildType);
    }
    




}
