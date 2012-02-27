package com.wixpress.ci.teamcity.domain;

import jetbrains.buildServer.serverSide.SBuildType;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author yoav
 * @since 2/22/12
 */
public class BuildDependenciesResult extends MavenDependenciesResult {

    private List<MBuildPlanItem> buildPlan;

    public BuildDependenciesResult(MavenDependenciesResult mavenResult, SBuildType buildType) {
        super(mavenResult.getResultType(), mavenResult.getModule(), mavenResult.getFullTrace());
        this.buildPlan = newArrayList();
    }

    public BuildDependenciesResult(MavenDependenciesResult mavenResult, SBuildType buildType, List<MBuildPlanItem> buildPlan) {
        super(mavenResult.getResultType(), mavenResult.getModule(), mavenResult.getFullTrace());
        this.buildPlan = buildPlan;
    }

    public List<MBuildPlanItem> getBuildPlan() {
        return buildPlan;
    }

    public void setBuildPlan(List<MBuildPlanItem> buildPlan) {
        this.buildPlan = buildPlan;
    }
}
