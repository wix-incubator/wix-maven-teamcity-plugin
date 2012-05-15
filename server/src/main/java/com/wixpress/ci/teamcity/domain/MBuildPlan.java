package com.wixpress.ci.teamcity.domain;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Igalh
 * Date: 5/15/12
 * Time: 2:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class MBuildPlan {
    List<MBuildPlanItem> buildPlanItems;

    public MBuildPlan(){}

    public MBuildPlan(List<MBuildPlanItem> buildPlanItems) {
        this.buildPlanItems = buildPlanItems;
    }

    public List<MBuildPlanItem> getBuildPlanItems() {
        return buildPlanItems;
    }

    public void setBuildPlanItems(List<MBuildPlanItem> buildPlanItems) {
        this.buildPlanItems = buildPlanItems;
    }
}
