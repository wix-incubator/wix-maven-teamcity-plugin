package com.wixpress.ci.teamcity.mavenAnalyzer;

import com.wixpress.ci.teamcity.domain.LogMessage;
import com.wixpress.ci.teamcity.domain.LogMessageType;
import com.wixpress.ci.teamcity.domain.MModule;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
* @author yoav
* @since 2/19/12
*/
public class DependenciesResult {
    private ResultType resultType;
    private MModule module;
    private List<LogMessage> fullTrace = newArrayList();

    public DependenciesResult(ResultType resultType) {
        this.resultType = resultType;
    }

    public DependenciesResult(MModule module) {
        this.resultType = ResultType.current;
        this.module = module;
    }

    public DependenciesResult(Exception exception) {
        this.resultType = ResultType.exception;
        LogMessage logMessage = new LogMessage(exception.getMessage(), LogMessageType.error, exception);
        fullTrace.add(logMessage);
    }

    public DependenciesResult(List<LogMessage> fullTrace) {
        this.resultType = ResultType.exception;
        this.fullTrace = fullTrace;
    }

    public DependenciesResult(ResultType resultType, MModule module) {
        this.resultType = resultType;
        this.module = module;
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
