package com.wixpress.ci.teamcity.mavenAnalyzer.dao;

import jetbrains.buildServer.serverSide.SBuildType;

import java.io.IOException;

/**
 * @author yoav
 * @since 2/23/12
 */
public interface DependenciesDao {
    BuildTypeDependenciesStorage load(SBuildType buildType) throws IOException;

    void save(BuildTypeDependenciesStorage storage, SBuildType buildType) throws IOException;
}
