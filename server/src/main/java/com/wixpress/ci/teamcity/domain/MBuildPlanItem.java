package com.wixpress.ci.teamcity.domain;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.Date;

/**
 * @author yoav
 * @since 2/22/12
 */
public class MBuildPlanItem {

    private BuildTypeId buildTypeId = new BuildTypeId();
    private boolean needsBuild;
    private String description;
    @JsonIgnore
    private Date latestBuildStart;
    @JsonIgnore
    private boolean hasPendingChanges;
    @JsonIgnore
    private boolean unknown;

    public MBuildPlanItem() {
    }

    public MBuildPlanItem(BuildTypeId buildTypeId) {
        this.buildTypeId = buildTypeId;
    }
    
    public MBuildPlanItem withLastBuildStart(Date lastBuildStart) {
        this.latestBuildStart = lastBuildStart;
        return this;
    }

    public MBuildPlanItem withPendingChanges(boolean hasPendingChanges) {
        this.hasPendingChanges = hasPendingChanges;
        return this;
    }

    public MBuildPlanItem unknown() {
        this.unknown = true;
        return this;
    }

    public MBuildPlanItem needsBuild(String description) {
        this.needsBuild = true;
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("MBuildPlanItem(")
                .append(buildTypeId)
                .append(",")
                .append(needsBuild ? "needsBuild - " + description : "ok")
                .append(")")
                .toString();
    }

    public BuildTypeId getBuildTypeId() {
        return buildTypeId;
    }

    public void setBuildTypeId(BuildTypeId buildTypeId) {
        this.buildTypeId = buildTypeId;
    }

    public Date getLatestBuildStart() {
        return latestBuildStart;
    }

    public void setLatestBuildStart(Date latestBuildStart) {
        this.latestBuildStart = latestBuildStart;
    }

    public boolean isHasPendingChanges() {
        return hasPendingChanges;
    }

    public void setHasPendingChanges(boolean hasPendingChanges) {
        this.hasPendingChanges = hasPendingChanges;
    }

    public boolean isUnknown() {
        return unknown;
    }

    public void setUnknown(boolean unknown) {
        this.unknown = unknown;
    }

    public boolean isNeedsBuild() {
        return needsBuild;
    }

    public void setNeedsBuild(boolean needsBuild) {
        this.needsBuild = needsBuild;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
