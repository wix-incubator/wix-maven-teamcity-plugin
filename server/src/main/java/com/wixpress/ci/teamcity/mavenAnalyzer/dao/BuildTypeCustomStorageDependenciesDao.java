package com.wixpress.ci.teamcity.mavenAnalyzer.dao;

import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuildType;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * @author yoav
 * @since 2/23/12
 */
public class BuildTypeCustomStorageDependenciesDao implements DependenciesDao {
    
    public static final String DEPENDENCIES_STORAGE = "com.wixpress.dependencies-storage";
    public static final String BUILD_DEPENDENCIES = "build-dependencies";
    private ObjectMapper objectMapper;

    public BuildTypeCustomStorageDependenciesDao(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public BuildTypeDependenciesStorage load(SBuildType buildType) throws IOException {
        CustomDataStorage customDataStorage = buildType.getCustomDataStorage(DEPENDENCIES_STORAGE);
        String serialized = customDataStorage.getValue(BUILD_DEPENDENCIES);
        if (serialized == null)
            return null;
        return objectMapper.readValue(serialized, BuildTypeDependenciesStorage.class);
    }

    public void save(BuildTypeDependenciesStorage storage, SBuildType buildType) throws IOException {
        String serializedModule = objectMapper.writeValueAsString(storage);
        buildType.getCustomDataStorage(DEPENDENCIES_STORAGE).putValue(BUILD_DEPENDENCIES, serializedModule);
    }
}
