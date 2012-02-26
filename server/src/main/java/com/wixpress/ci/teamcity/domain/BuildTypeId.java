package com.wixpress.ci.teamcity.domain;

import jetbrains.buildServer.serverSide.SBuildType;

/**
* @author yoav
* @since 2/26/12
*/
public class BuildTypeId {
    private String name;
    private String projectName;
    private String buildTypeId;
    private String projectId;

    public BuildTypeId() {
    }

    public BuildTypeId(SBuildType buildType) {
        this.name = buildType.getName();
        this.buildTypeId = buildType.getBuildTypeId();
        this.projectId = buildType.getProjectId();
        this.projectName = buildType.getProjectName();
    }

    public BuildTypeId(String name, String projectName, String buildTypeId, String projectId) {
        this.name = name;
        this.projectName = projectName;
        this.buildTypeId = buildTypeId;
        this.projectId = projectId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BuildTypeId buildTypeId = (BuildTypeId) o;

        if (this.buildTypeId != null ? !this.buildTypeId.equals(buildTypeId.buildTypeId) : buildTypeId.buildTypeId != null)
            return false;
        if (name != null ? !name.equals(buildTypeId.name) : buildTypeId.name != null) return false;
        if (projectId != null ? !projectId.equals(buildTypeId.projectId) : buildTypeId.projectId != null) return false;
        if (projectName != null ? !projectName.equals(buildTypeId.projectName) : buildTypeId.projectName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (projectName != null ? projectName.hashCode() : 0);
        result = 31 * result + (buildTypeId != null ? buildTypeId.hashCode() : 0);
        result = 31 * result + (projectId != null ? projectId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s:%s (%s:%s)", projectName, name, projectId, buildTypeId);
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
