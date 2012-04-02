package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.wixpress.ci.teamcity.DependenciesAnalyzer;
import com.wixpress.ci.teamcity.domain.*;
import com.wixpress.ci.teamcity.mavenAnalyzer.MavenBuildTypeDependenciesAnalyzer;
import com.wixpress.ci.teamcity.mavenAnalyzer.dao.DependenciesDao;
import com.wixpress.ci.teamcity.teamCityAnalyzer.entity.BuildTypeDependencies;
import jetbrains.buildServer.serverSide.SBuildType;

import java.io.IOException;
import java.util.List;

/**
 * analyzes the dependencies between TeamCity build configurations based on Maven dependencies
 * @author yoav
 * @since 2/22/12
 */
public class BuildTypesDependencyAnalyzer implements DependenciesAnalyzer<BuildDependenciesResult>{

    private MavenBuildTypeDependenciesAnalyzer mavenBuildAnalyzer;
    private BuildTypeDependenciesDecorator dependenciesDecorator;
    private BuildPlanAnalyzer buildPlanAnalyzer;
    private BuildTypeDependenciesExtractor buildTypeDependenciesExtractor;
    private DependenciesDao dependenciesDao;

    public BuildTypesDependencyAnalyzer(MavenBuildTypeDependenciesAnalyzer mavenBuildAnalyzer,
                                        BuildTypeDependenciesDecorator dependenciesDecorator,
                                        BuildPlanAnalyzer buildPlanAnalyzer,
                                        BuildTypeDependenciesExtractor buildTypeDependenciesExtractor,
                                        DependenciesDao dependenciesDao) {
        this.mavenBuildAnalyzer = mavenBuildAnalyzer;
        this.dependenciesDecorator = dependenciesDecorator;
        this.buildPlanAnalyzer = buildPlanAnalyzer;
        this.buildTypeDependenciesExtractor = buildTypeDependenciesExtractor;
        this.dependenciesDao = dependenciesDao;
    }

    public BuildDependenciesResult getBuildDependencies(SBuildType buildType) {
        return getBuildDependencies(buildType, true);
    }

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

    public BuildDependenciesResult forceAnalyzeDependencies(SBuildType buildType) {
        MavenDependenciesResult mavenResult = mavenBuildAnalyzer.forceAnalyzeDependencies(buildType);
        return toBuildDependenciesResult(mavenResult, buildType);
    }

    public CollectProgress getProgress(String buildTypeId, Integer position) {
        return mavenBuildAnalyzer.getProgress(buildTypeId, position);
    }

    private BuildDependenciesResult analyzeBuildTypeDependencies(SBuildType buildType, MavenDependenciesResult mavenResult) {
        MavenDependenciesResult mavenDependenciesResult = dependenciesDecorator.decorateWithBuildTypesAnalysis(mavenResult, buildType);
        try {
            BuildTypeDependencies buildTypeDependencies = getBuildTypeDependencies(mavenDependenciesResult.getModule(), buildType);
            List<MBuildPlanItem> buildPlan = buildPlanAnalyzer.getBuildPlan(buildTypeDependencies);
            return new BuildDependenciesResult(mavenDependenciesResult, buildType, buildPlan);
        }
        catch (Exception e) {
            BuildDependenciesResult buildDependenciesResult = new BuildDependenciesResult(mavenDependenciesResult, buildType);
            buildDependenciesResult.getFullTrace().add(new LogMessage("failed to sort build dependencies", LogMessageType.error, e));
            return buildDependenciesResult;
        }
    }

    private BuildTypeDependencies getBuildTypeDependencies(MModule module, SBuildType buildType) throws IOException {
        BuildTypeId currentBuildId = new BuildTypeId(buildType);
        BuildTypeDependencies buildTypeDependencies = buildTypeDependenciesExtractor.extract(module, currentBuildId);
        dependenciesDao.saveBuildDependencies(buildTypeDependencies, buildType);
        return buildTypeDependencies;
    }

    private BuildDependenciesResult toBuildDependenciesResult(MavenDependenciesResult mavenResult, SBuildType buildType) {
        return new BuildDependenciesResult(mavenResult, buildType);
    }
    




}
