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

    public MBuildPlanItem() {
    }

    public MBuildPlanItem(BuildTypeId buildTypeId) {
        this.buildTypeId = buildTypeId;
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
