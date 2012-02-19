package com.wixpress.ci.teamcity.dependenciesTab;

import com.wixpress.ci.teamcity.dependenciesTab.mavenAnalyzer.TeamCityBuildMavenDependenciesAnalyzer;
import jetbrains.buildServer.serverSide.SBuildServer;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * @author yoav
 * @since 2/18/12
 */
public class TeamCityBuildDependenciesAnalyzer {
    
    private static final String DEPENDENCIES_STORAGE = "com.wixpress.dependencies-storage";
    private static final String BUILD_DEPENDENCIES = "build-dependencies";
    private SBuildServer server;
    private ObjectMapper objectMapper;
    private TeamCityBuildMavenDependenciesAnalyzer dependenciesAnalyzer;
    private Map<String, String> runningCollections = newHashMap();
    
//    public BuildDependenciesResponse getBuildDependencies(String buildTypeId) {
//        SBuildType buildType = server.getProjectManager().findBuildTypeById(buildTypeId);
//        String buildDependenciesJson = buildType.getCustomDataStorage(DEPENDENCIES_STORAGE).getValue(BUILD_DEPENDENCIES);
//        BuildDependencies buildDependencies = objectMapper.readValue(buildDependenciesJson, BuildDependencies.class);
//        if (needRefreshingBuildDependencies(buildType, buildDependencies)) {
//            if (runningCollections.containsKey(buildTypeId))
//                return new BuildDependenciesResponse(projectDependenciesAnalyzer.)
//            String runningCollectionKey = projectDependenciesAnalyzer.collectDependencies(buildType);
//
//        }
//    }
}
