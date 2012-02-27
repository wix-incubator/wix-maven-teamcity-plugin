package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.wixpress.ci.teamcity.DependenciesAnalyzer;
import com.wixpress.ci.teamcity.domain.*;
import com.wixpress.ci.teamcity.mavenAnalyzer.MavenBuildTypeDependenciesAnalyzer;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;

import java.util.List;

/**
 * analyzes the dependencies between TeamCity build configurations based on Maven dependencies
 * @author yoav
 * @since 2/22/12
 */
public class BuildTypesDependencyAnalyzer implements DependenciesAnalyzer<BuildDependenciesResult>{

    private MavenBuildTypeDependenciesAnalyzer mavenBuildAnalyzer;
    private ProjectManager projectManager;
    private BuildTypeDependenciesDecorator dependenciesDecorator;
    private BuildTypeDependenciesSorter dependenciesSorter;

    public BuildTypesDependencyAnalyzer(MavenBuildTypeDependenciesAnalyzer mavenBuildAnalyzer, ProjectManager projectManager, BuildTypeDependenciesDecorator dependenciesDecorator, BuildTypeDependenciesSorter dependenciesSorter) {
        this.mavenBuildAnalyzer = mavenBuildAnalyzer;
        this.projectManager = projectManager;
        this.dependenciesDecorator = dependenciesDecorator;
        this.dependenciesSorter = dependenciesSorter;
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
            return analyzeBuildTypeDependencies(buildType, mavenResult);
        else
            return toBuildDependenciesResult(mavenResult, buildType);
    }

    public BuildDependenciesResult analyzeDependencies(SBuildType buildType) {
        MavenDependenciesResult mavenResult = mavenBuildAnalyzer.analyzeDependencies(buildType);
        if (mavenResult.getResultType().hasDependencies())
            return analyzeBuildTypeDependencies(buildType, mavenResult);
        else
            return toBuildDependenciesResult(mavenResult, buildType);
    }

    private BuildDependenciesResult analyzeBuildTypeDependencies(SBuildType buildType, MavenDependenciesResult mavenResult) {
        MavenDependenciesResult mavenDependenciesResult = dependenciesDecorator.decorateWithBuildTypesAnalysis(mavenResult, buildType);
        try {
            List<BuildTypeId> sortedBuildTypeDependencies = dependenciesSorter.sortBuildTypes(mavenDependenciesResult.getModule(), new BuildTypeId(buildType));
            return new BuildDependenciesResult(mavenDependenciesResult, buildType, sortedBuildTypeDependencies);
        }
        catch (IllegalStateException e) {
            BuildDependenciesResult buildDependenciesResult = new BuildDependenciesResult(mavenDependenciesResult, buildType);
            buildDependenciesResult.getFullTrace().add(new LogMessage("failed to sort build dependencies", LogMessageType.error, e));
            return buildDependenciesResult;
        }
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
