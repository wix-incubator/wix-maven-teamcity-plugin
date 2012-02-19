package com.wixpress.ci.teamcity.dependenciesTab.mavenAnalyzer;

import com.wixpress.ci.teamcity.domain.MModule;

/**
* @author yoav
* @since 2/19/12
*/
public class DependenciesResult {
    private ResultType resultType;
    private MModule module;
    private Exception exception;

    public DependenciesResult(ResultType resultType) {
        this.resultType = resultType;
    }

    public DependenciesResult(MModule module) {
        this.resultType = ResultType.current;
        this.module = module;
    }

    public DependenciesResult(Exception exception) {
        this.resultType = ResultType.exception;
        this.exception = exception;
    }

    public DependenciesResult(ResultType needsRefresh, MModule module) {
        this.resultType = resultType;
        this.module = module;
    }

    public ResultType getResultType() {
        return resultType;
    }

    public MModule getModule() {
        return module;
    }

    public Exception getException() {
        return exception;
    }
}
