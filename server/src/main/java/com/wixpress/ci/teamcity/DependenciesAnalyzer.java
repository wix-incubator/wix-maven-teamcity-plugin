package com.wixpress.ci.teamcity;

import com.wixpress.ci.teamcity.domain.CollectProgress;
import com.wixpress.ci.teamcity.domain.DependenciesResult;
import jetbrains.buildServer.serverSide.SBuildType;

/**
 * @author yoav
 * @since 2/22/12
 */
public interface DependenciesAnalyzer<Result extends DependenciesResult> {
    Result getBuildDependencies(SBuildType buildType);

    Result analyzeDependencies(SBuildType buildType);

    Result forceAnalyzeDependencies(SBuildType buildType);

    CollectProgress getProgress(String buildTypeId, Integer position);
}
