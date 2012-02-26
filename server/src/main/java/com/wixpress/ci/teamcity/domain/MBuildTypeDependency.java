package com.wixpress.ci.teamcity.domain;

import jetbrains.buildServer.serverSide.SBuildType;

/**
 * @author yoav
 * @since 2/22/12
 */
public class MBuildTypeDependency extends MDependency {

    private BuildTypeId buildTypeId;

    public MBuildTypeDependency() {
    }

    public MBuildTypeDependency(MDependency artifact, BuildTypeId buildTypeId) {
        super(artifact);
        this.buildTypeId = buildTypeId;
    }

    public BuildTypeId getBuildTypeId() {
        return buildTypeId;
    }

    public void setBuildTypeId(BuildTypeId buildTypeId) {
        this.buildTypeId = buildTypeId;
    }
}
