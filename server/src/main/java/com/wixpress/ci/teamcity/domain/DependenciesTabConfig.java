package com.wixpress.ci.teamcity.domain;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author yoav
 * @since 3/5/12
 */
public class DependenciesTabConfig {
    public List<String> getCommitsToIgnore() {
        return commitsToIgnore;
    }

    public void setCommitsToIgnore(List<String> commitsToIgnore) {
        this.commitsToIgnore = commitsToIgnore;
    }

    private List<String> commitsToIgnore = newArrayList();
}
