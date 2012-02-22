package com.wixpress.ci.teamcity.domain;

import jetbrains.buildServer.serverSide.SBuildType;

/**
 * @author yoav
 * @since 2/22/12
 */
public class MBuildTypeDependency extends MDependency {

    private String name;
    private String projectName;
    private String buildTypeId;
    private String projectId;

    public MBuildTypeDependency() {
    }

    public MBuildTypeDependency(MDependency artifact, String name, String buildTypeId, String projectName, String projectId) {
        super(artifact);
        this.name = name;
        this.projectName = projectName;
        this.buildTypeId = buildTypeId;
        this.projectId = projectId;
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
}
