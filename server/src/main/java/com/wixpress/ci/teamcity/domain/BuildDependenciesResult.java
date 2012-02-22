package com.wixpress.ci.teamcity.domain;

import jetbrains.buildServer.serverSide.SBuildType;

/**
 * @author yoav
 * @since 2/22/12
 */
public class BuildDependenciesResult extends MavenDependenciesResult {

    private MBuildType buildType;

    public BuildDependenciesResult(MavenDependenciesResult mavenResult, SBuildType buildType) {
        super(mavenResult.getResultType(), mavenResult.getModule(), mavenResult.getFullTrace());
        this.buildType = new MBuildType(buildType);
    }

    public BuildDependenciesResult(MavenDependenciesResult mavenResult, MModule mModule, SBuildType buildType) {
        super(mavenResult.getResultType(), mModule, mavenResult.getFullTrace());
        this.buildType = new MBuildType(buildType);
    }

    public MBuildType getBuildType() {
        return buildType;
    }

    public void setBuildType(MBuildType buildType) {
        this.buildType = buildType;
    }
}
