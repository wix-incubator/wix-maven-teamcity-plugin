package com.wixpress.ci.teamcity.domain;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
* @author yoav
* @since 2/19/12
*/
public class MavenDependenciesResult implements DependenciesResult {
    private ResultType resultType;
    private MModule module;
    private List<LogMessage> fullTrace = newArrayList();

    public MavenDependenciesResult(ResultType resultType) {
        this.resultType = resultType;
    }

    public MavenDependenciesResult(MModule module) {
        this.resultType = ResultType.current;
        this.module = module;
    }

    public MavenDependenciesResult(Exception exception) {
        this.resultType = ResultType.exception;
        LogMessage logMessage = new LogMessage(exception.getMessage(), LogMessageType.error, exception);
        fullTrace.add(logMessage);
    }

    public MavenDependenciesResult(List<LogMessage> fullTrace) {
        this.resultType = ResultType.exception;
        this.fullTrace = fullTrace;
    }

    public MavenDependenciesResult(ResultType resultType, MModule module) {
        this.resultType = resultType;
        this.module = module;
    }

    public MavenDependenciesResult(ResultType resultType, MModule module, List<LogMessage> fullTrace) {
        this.resultType = resultType;
        this.module = module;
        this.fullTrace = fullTrace;
    }

    public ResultType getResultType() {
        return resultType;
    }

    public MModule getModule() {
        return module;
    }

    public List<LogMessage> getFullTrace() {
        return fullTrace;
    }

    public void setFullTrace(List<LogMessage> fullTrace) {
        this.fullTrace = fullTrace;
    }
}
