package com.wixpress.ci.teamcity.mavenAnalyzer.dao;

import com.wixpress.ci.teamcity.teamCityAnalyzer.entity.BuildTypeDependencies;
import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuildType;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * @author yoav
 * @since 2/23/12
 */
public class BuildTypeCustomStorageDependenciesDao implements DependenciesDao {

    public static final String DEPENDENCIES_STORAGE = "com.wixpress.dependencies-storage";
    public static final String MODULE_DEPENDENCIES = "build-dependencies";
    public static final String BUILD_DEPENDENCIES = "build-type-dependencies";
    private ObjectMapper objectMapper;

    public BuildTypeCustomStorageDependenciesDao(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ModuleDependenciesStorage loadModuleDependencies(SBuildType buildType) {
        try {
            CustomDataStorage customDataStorage = buildType.getCustomDataStorage(DEPENDENCIES_STORAGE);
            String serialized = customDataStorage.getValue(MODULE_DEPENDENCIES);
            if (serialized == null)
                return null;
            return objectMapper.readValue(serialized, ModuleDependenciesStorage.class);
        } catch (Exception e) {
            throw new BuildTypeCustomStorageDependenciesDaoException("failed reading module dependencies for build type [%s/%s]", e, buildType.getProjectId(), buildType.getBuildTypeId());
        }
    }

    public void saveModuleDependencies(ModuleDependenciesStorage storage, SBuildType buildType)  {
        try {
            String serializedModule = objectMapper.writeValueAsString(storage);
            buildType.getCustomDataStorage(DEPENDENCIES_STORAGE).putValue(MODULE_DEPENDENCIES, serializedModule);
        } catch (Exception e) {
            throw new BuildTypeCustomStorageDependenciesDaoException("failed saving module dependencies for build type [%s/%s]", e, buildType.getProjectId(), buildType.getBuildTypeId());
        }
    }

    public BuildTypeDependencies loadBuildDependencies(SBuildType buildType) {
        try {
            CustomDataStorage customDataStorage = buildType.getCustomDataStorage(DEPENDENCIES_STORAGE);
            String serialized = customDataStorage.getValue(BUILD_DEPENDENCIES);
            if (serialized == null)
                return null;
            return objectMapper.readValue(serialized, BuildTypeDependencies.class);
        } catch (Exception e) {
            throw new BuildTypeCustomStorageDependenciesDaoException("failed reading build dependencies for build type [%s/%s]", e, buildType.getProjectId(), buildType.getBuildTypeId());
        }
    }

    public void saveBuildDependencies(BuildTypeDependencies buildTypeDependencies, SBuildType buildType) {
        try {
            String serializedModule = objectMapper.writeValueAsString(buildTypeDependencies);
            buildType.getCustomDataStorage(DEPENDENCIES_STORAGE).putValue(BUILD_DEPENDENCIES, serializedModule);
        } catch (Exception e) {
            throw new BuildTypeCustomStorageDependenciesDaoException("failed saving build dependencies for build type [%s/%s]", e, buildType.getProjectId(), buildType.getBuildTypeId());
        }
    }
}
