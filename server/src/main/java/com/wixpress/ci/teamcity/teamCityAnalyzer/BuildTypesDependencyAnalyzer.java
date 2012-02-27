package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.wixpress.ci.teamcity.DependenciesAnalyzer;
import com.wixpress.ci.teamcity.domain.*;
import com.wixpress.ci.teamcity.mavenAnalyzer.MavenBuildTypeDependenciesAnalyzer;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

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
            List<BuildTypeId> sortedBuildTypeDependencies = dependenciesSorter.sortBuildTypes(mavenDependenciesResult.getModule(), new BuildTypeId(buildType));
            List<MBuildPlanItem> buildPlan = computeBuildPlan(sortedBuildTypeDependencies);
            return new BuildDependenciesResult(mavenDependenciesResult, buildType, buildPlan);
        }
        catch (IllegalStateException e) {
            BuildDependenciesResult buildDependenciesResult = new BuildDependenciesResult(mavenDependenciesResult, buildType);
            buildDependenciesResult.getFullTrace().add(new LogMessage("failed to sort build dependencies", LogMessageType.error, e));
            return buildDependenciesResult;
        }
    }

    private List<MBuildPlanItem> computeBuildPlan(List<BuildTypeId> sortedBuildTypeDependencies) {
        List<MBuildPlanItem> buildPlan = newArrayList();
        for (BuildTypeId buildTypeId: sortedBuildTypeDependencies) {
            buildPlan.add(createBuildPlanItem(buildTypeId));
        }
        markItemsInNeedOfBuild(buildPlan);
        return buildPlan;
    }

    private void markItemsInNeedOfBuild(List<MBuildPlanItem> buildPlan) {
        for (int i=buildPlan.size()-1, j=i-1; i > 0; i--, j--) {
            MBuildPlanItem next = buildPlan.get(i);
            MBuildPlanItem current = buildPlan.get(j);
            if (next.isNeedsBuild())
                current.needsBuild(String.format("Dependency [%s:%s] require building", next.getBuildTypeId().getProjectName(), next.getBuildTypeId().getName()));
            else if (current.getLatestBuildStart() == null)
                current.needsBuild("No successful build found");
            else if (next.getLatestBuildStart() != null && current.getLatestBuildStart().compareTo(next.getLatestBuildStart())< 0)
                current.needsBuild(String.format("Dependency [%s:%s] last build is newer", next.getBuildTypeId().getProjectName(), next.getBuildTypeId().getName()));
            else if (current.isHasPendingChanges())
                current.needsBuild("Has pending changes");
        }
    }

    private MBuildPlanItem createBuildPlanItem(BuildTypeId buildTypeId) {
        SBuildType buildType = projectManager.findBuildTypeById(buildTypeId.getBuildTypeId());
        MBuildPlanItem item;
        if (buildType != null) {
            SFinishedBuild lastSuccessfulBuild = buildType.getLastChangesSuccessfullyFinished();
            if (lastSuccessfulBuild != null)
                item = new MBuildPlanItem(buildTypeId)
                        .withLastBuildStart(lastSuccessfulBuild.getClientStartDate())
                        .withPendingChanges(buildType.getPendingChanges().size() > 0);
            else
                item = new MBuildPlanItem(buildTypeId)
                        .withPendingChanges(buildType.getPendingChanges().size() > 0);
        }
        else
            item = new MBuildPlanItem(buildTypeId)
                    .unknown();
        return item;
    }


    private BuildDependenciesResult toBuildDependenciesResult(MavenDependenciesResult mavenResult, SBuildType buildType) {
        return new BuildDependenciesResult(mavenResult, buildType);
    }
    




}
