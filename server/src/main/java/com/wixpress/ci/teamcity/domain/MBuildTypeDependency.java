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

    public MBuildTypeDependency(String groupId, String artifactId, String version, String scope, boolean optional, String name, String projectName, String buildTypeId, String projectId) {
        super(groupId, artifactId, version, scope, optional);
        this.buildTypeId = new BuildTypeId(name, projectName, buildTypeId, projectId);
    }

    public MBuildTypeDependency(String groupId, String artifactId, String version, String scope, boolean optional, String name, String projectName, String buildTypeId, String projectId, boolean isKnown) {
        super(groupId, artifactId, version, scope, optional);
        this.buildTypeId = new BuildTypeId(name, projectName, buildTypeId, projectId, isKnown);
    }

    public BuildTypeId getBuildTypeId() {
        return buildTypeId;
    }

    public void setBuildTypeId(BuildTypeId buildTypeId) {
        this.buildTypeId = buildTypeId;
    }
}
