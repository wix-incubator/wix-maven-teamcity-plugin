package com.wixpress.ci.teamcity.domain;

import jetbrains.buildServer.serverSide.SBuildType;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author yoav
 * @since 2/22/12
 */
public class MBuildType implements Tree<MBuildType>{

    private String name;
    private String projectName;
    private String buildTypeId;
    private String projectId;
    private List<MBuildType> dependencies = newArrayList();

    public MBuildType() {

    }

    public MBuildType(String name, String projectName, String buildTypeId, String projectId) {
        this.name = name;
        this.projectName = projectName;
        this.buildTypeId = buildTypeId;
        this.projectId = projectId;
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
                .append(projectName)
                .append(":")
                .append(name)
                .append("(")
                .append(projectId)
                .append("-")
                .append(buildTypeId)
                .append(")")
                .toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getBuildTypeId() {
        return buildTypeId;
    }

    public void setBuildTypeId(String buildTypeId) {
        this.buildTypeId = buildTypeId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
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
