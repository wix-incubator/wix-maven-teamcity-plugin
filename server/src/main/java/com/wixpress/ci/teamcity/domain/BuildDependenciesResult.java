package com.wixpress.ci.teamcity.domain;

import jetbrains.buildServer.serverSide.SBuildType;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author yoav
 * @since 2/22/12
 */
public class BuildDependenciesResult extends MavenDependenciesResult {

    private MBuildType buildType;
    private List<BuildTypeId> sortedDependencies;

    public BuildDependenciesResult(MavenDependenciesResult mavenResult, SBuildType buildType) {
        super(mavenResult.getResultType(), mavenResult.getModule(), mavenResult.getFullTrace());
        this.buildType = new MBuildType(buildType);
        this.sortedDependencies = newArrayList();
    }

    public BuildDependenciesResult(MavenDependenciesResult mavenResult, SBuildType buildType, List<BuildTypeId> sortedDependencies) {
        super(mavenResult.getResultType(), mavenResult.getModule(), mavenResult.getFullTrace());
        this.buildType = new MBuildType(buildType);
        this.sortedDependencies = sortedDependencies;
    }

    public MBuildType getBuildType() {
        return buildType;
    }

    public void setBuildType(MBuildType buildType) {
        this.buildType = buildType;
    }

    public List<BuildTypeId> getSortedDependencies() {
        return sortedDependencies;
    }

    public void setSortedDependencies(List<BuildTypeId> sortedDependencies) {
        this.sortedDependencies = sortedDependencies;
    }
}
