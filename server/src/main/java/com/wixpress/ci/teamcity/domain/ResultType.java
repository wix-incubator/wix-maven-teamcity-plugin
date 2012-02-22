package com.wixpress.ci.teamcity.domain;

/**
* @author yoav
* @since 2/19/12
*/
public enum ResultType {
    current(true),
    exception(false),
    runningAsync(false),
    needsRefresh(true),
    notRun(false);
    boolean hasDependencies;

    private ResultType(boolean hasDependencies) {
        this.hasDependencies = hasDependencies;
    }

    public boolean hasDependencies() {
        return hasDependencies;
    }
}
