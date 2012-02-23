package com.wixpress.ci.teamcity.domain;

import java.util.List;

/**
 * @author yoav
 * @since 2/22/12
 */
public interface DependenciesResult {
    public ResultType getResultType();
    public List<LogMessage> getFullTrace();

    MModule getModule();
}
