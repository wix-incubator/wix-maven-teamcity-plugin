package com.wixpress.ci.teamcity.domain;

import jetbrains.buildServer.serverSide.SBuildType;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author yoav
 * @since 2/22/12
 */
public class MBuildType implements Tree<MBuildType>{

    private BuildTypeId buildTypeId = new BuildTypeId();
    private List<MBuildType> dependencies = newArrayList();

    public MBuildType() {

    }

    public MBuildType(String name, String projectName, String buildTypeId, String projectId) {
        this.buildTypeId = new BuildTypeId(name, projectName, buildTypeId, projectId);
    }

    public MBuildType(SBuildType buildType) {
        this(buildType.getName(), buildType.getProjectName(), buildType.getBuildTypeId(), buildType.getProjectId());
    }

    public MBuildType(BuildTypeId buildTypeId) {
        this(buildTypeId.getName(), buildTypeId.getProjectName(), buildTypeId.getBuildTypeId(), buildTypeId.getProjectId());
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("MBuildType(")
                .append(buildTypeId.getProjectName())
                .append(":")
                .append(buildTypeId.getName())
                .append("(")
                .append(buildTypeId.getProjectId())
                .append("-")
                .append(buildTypeId.getBuildTypeId())
                .append(")")
                .toString();
    }

    public BuildTypeId getBuildTypeId() {
        return buildTypeId;
    }

    public void setBuildTypeId(BuildTypeId buildTypeId) {
        this.buildTypeId = buildTypeId;
    }

    public List<MBuildType> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<MBuildType> dependencies) {
        this.dependencies = dependencies;
    }

    public List<MBuildType> getChildren() {
        return getDependencies();
    }
}
