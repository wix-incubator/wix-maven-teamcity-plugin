package com.wixpress.ci.teamcity.teamCityAnalyzer.entity;

import com.wixpress.ci.teamcity.domain.BuildTypeId;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

/**
 * @author yoav
 * @since 4/2/12
 */
public class BuildTypeDependencies {
    private BuildTypeId buildTypeId;
    private Set<BuildTypeId> dependencies = newHashSet();

    public BuildTypeDependencies(BuildTypeId buildTypeId) {
        this.buildTypeId = buildTypeId;
    }

    public BuildTypeDependencies() {
    }

    public BuildTypeId getBuildTypeId() {
        return buildTypeId;
    }

    public void setBuildTypeId(BuildTypeId buildTypeId) {
        this.buildTypeId = buildTypeId;
    }

    public Set<BuildTypeId> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<BuildTypeId> dependencies) {
        this.dependencies = dependencies;
    }
}
