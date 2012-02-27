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
    private boolean isKnown;

    public BuildTypeId() {
    }

    public BuildTypeId(SBuildType buildType) {
        this(buildType.getName(), buildType.getProjectName(),buildType.getBuildTypeId(), buildType.getProjectId(), true);
    }

    public BuildTypeId(String name, String projectName, String buildTypeId, String projectId) {
        this(name, projectName,buildTypeId, projectId, true);
    }

    public BuildTypeId(String name, String projectName, String buildTypeId, String projectId, boolean isKnown) {
        this.name = name;
        this.projectName = projectName;
        this.buildTypeId = buildTypeId;
        this.projectId = projectId;
        this.isKnown = isKnown;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BuildTypeId that = (BuildTypeId) o;

        if (isKnown != that.isKnown) return false;
        if (buildTypeId != null ? !buildTypeId.equals(that.buildTypeId) : that.buildTypeId != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (projectId != null ? !projectId.equals(that.projectId) : that.projectId != null) return false;
        if (projectName != null ? !projectName.equals(that.projectName) : that.projectName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (projectName != null ? projectName.hashCode() : 0);
        result = 31 * result + (buildTypeId != null ? buildTypeId.hashCode() : 0);
        result = 31 * result + (projectId != null ? projectId.hashCode() : 0);
        result = 31 * result + (isKnown ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s%s:%s (%s:%s)", isKnown?"":"?", projectName, name, projectId, buildTypeId);
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

    public boolean isKnown() {
        return isKnown;
    }

    public void setKnown(boolean known) {
        isKnown = known;
    }
}
