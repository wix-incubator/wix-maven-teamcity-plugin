package com.wixpress.ci.teamcity.domain;


import java.util.ArrayList;
import java.util.List;

/**
 * @author yoav
 * @since 2/22/12
 */
public class MBuildPlanItem {

    private BuildTypeId buildTypeId = new BuildTypeId();
    private boolean needsBuild;
    private List<String> newerChildren;
    private String description;
    private boolean hasPendingChanges;
    private List<MBuildPlanItem> children;

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

    public List<MBuildPlanItem> getChildren() {
        return children;
    }

    public void setChildren(List<MBuildPlanItem> children) {
        this.children = children;
    }

    public boolean isHasPendingChanges() {
        return hasPendingChanges;
    }

    public void setHasPendingChanges(boolean hasPendingChanges) {
        this.hasPendingChanges = hasPendingChanges;
    }

    public List<String> getNewerChildren() {
        return newerChildren;
    }

    public void setNewerChildren(List<String> newerChildren) {
        this.newerChildren = newerChildren;
    }
}
