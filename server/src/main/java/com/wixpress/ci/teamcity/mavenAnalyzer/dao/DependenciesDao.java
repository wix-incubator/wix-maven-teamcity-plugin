package com.wixpress.ci.teamcity.mavenAnalyzer.dao;

import com.wixpress.ci.teamcity.teamCityAnalyzer.entity.BuildTypeDependencies;
import jetbrains.buildServer.serverSide.SBuildType;

import java.io.IOException;

/**
 * @author yoav
 * @since 2/23/12
 */
public interface DependenciesDao {

    ModuleDependenciesStorage loadModuleDependencies(SBuildType buildType);

    void saveModuleDependencies(ModuleDependenciesStorage storage, SBuildType buildType);

    BuildTypeDependencies loadBuildDependencies(SBuildType buildType);

    void saveBuildDependencies(BuildTypeDependencies buildTypeDependencies, SBuildType buildType);

}
