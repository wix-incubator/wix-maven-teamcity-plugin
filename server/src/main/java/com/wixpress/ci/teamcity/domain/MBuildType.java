package com.wixpress.ci.teamcity.domain;

import jetbrains.buildServer.serverSide.SBuildType;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author yoav
 * @since 2/22/12
 */
public class MBuildType {

    private String name;
    private String projectName;
    private String buildTypeId;
    private String projectId;
    private List<MBuildType> dependencies = newArrayList();

    public MBuildType() {
    }

    public MBuildType(SBuildType buildType) {
        this.name = buildType.getName();
        this.projectName = buildType.getProjectName();
        this.buildTypeId = buildType.getBuildTypeId();
        this.projectId = buildType.getProjectId();
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
}
