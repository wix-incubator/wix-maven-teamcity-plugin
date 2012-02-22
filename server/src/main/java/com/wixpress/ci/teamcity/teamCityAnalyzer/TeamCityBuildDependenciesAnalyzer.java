package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.wixpress.ci.teamcity.DependenciesAnalyzer;
import com.wixpress.ci.teamcity.domain.*;
import com.wixpress.ci.teamcity.mavenAnalyzer.TeamCityBuildMavenDependenciesAnalyzer;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;

import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.synchronizedBiMap;

/**
 * analyzes the dependencies between TeamCity build configurations based on Maven dependencies
 * @author yoav
 * @since 2/22/12
 */
public class TeamCityBuildDependenciesAnalyzer implements DependenciesAnalyzer<BuildDependenciesResult>{

    private TeamCityBuildMavenDependenciesAnalyzer mavenDependenciesAnalyzer;
    private ProjectManager projectManager;

    public TeamCityBuildDependenciesAnalyzer(TeamCityBuildMavenDependenciesAnalyzer mavenDependenciesAnalyzer, ProjectManager projectManager) {
        this.mavenDependenciesAnalyzer = mavenDependenciesAnalyzer;
        this.projectManager = projectManager;
    }

    /**
     * gets the BuildType dependencies as well as the maven dependencies of a project
     * @param buildType
     * @return
     */
    public BuildDependenciesResult getBuildDependencies(SBuildType buildType) {
        MavenDependenciesResult mavenResult = mavenDependenciesAnalyzer.getBuildDependencies(buildType);
        if (mavenResult.getResultType().hasDependencies())
            return decorateWithBuildTypesAnalysis(mavenResult, buildType);
        else
            return toBuildDependenciesResult(mavenResult, buildType);
    }

    public BuildDependenciesResult analyzeDependencies(SBuildType buildType) {
        MavenDependenciesResult mavenResult = mavenDependenciesAnalyzer.analyzeDependencies(buildType);
        if (mavenResult.getResultType().hasDependencies())
            return decorateWithBuildTypesAnalysis(mavenResult, buildType);
        else
            return toBuildDependenciesResult(mavenResult, buildType);
    }

    public BuildDependenciesResult forceAnalyzeDependencies(SBuildType buildType) {
        MavenDependenciesResult mavenResult = mavenDependenciesAnalyzer.forceAnalyzeDependencies(buildType);
        return toBuildDependenciesResult(mavenResult, buildType);
    }

    public CollectProgress getProgress(String buildTypeId, Integer position) {
        return mavenDependenciesAnalyzer.getProgress(buildTypeId, position);
    }

    private BuildDependenciesResult decorateWithBuildTypesAnalysis(MavenDependenciesResult mavenResult, SBuildType buildType) {
        RequestCache requestCache = new RequestCache();
        requestCache.put(buildType, mavenResult);
        Map<ArtifactId, BuildTypeId> artifactBuildMapping = readArtifactModuleMapping(requestCache);
        return new BuildDependenciesResult(mavenResult, decorateDependenciesWithBuilds(mavenResult.getModule(), artifactBuildMapping), buildType);
    }

    private BuildDependenciesResult toBuildDependenciesResult(MavenDependenciesResult mavenResult, SBuildType buildType) {
        return new BuildDependenciesResult(mavenResult, buildType);
    }
    
    private Map<ArtifactId, BuildTypeId> readArtifactModuleMapping(RequestCache requestCache) {
        Map<ArtifactId, BuildTypeId> artifactBuildMapping = newHashMap();
        for (SBuildType buildType: projectManager.getActiveBuildTypes()) {
            MavenDependenciesResult buildDependencies = requestCache.getBuildDependencies(buildType);
            if (buildDependencies.getResultType() == ResultType.current ||
                    buildDependencies.getResultType() == ResultType.needsRefresh)
                extractArtifactModuleMapping(buildDependencies.getModule(), artifactBuildMapping, buildType);
        }
        return artifactBuildMapping;
    }
    
    private MModule decorateDependenciesWithBuilds(MModule module, Map<ArtifactId, BuildTypeId> artifactBuildMapping) {
        MModule decoratedModule = new MModule(module);
        for (MModule subModule: module.getSubModules())
            decoratedModule.getSubModules().add(decorateDependenciesWithBuilds(subModule, artifactBuildMapping));
        decoratedModule.setDependencyTree(decorateDependenciesWithBuilds(module.getDependencyTree(), artifactBuildMapping));
        return decoratedModule;
    }

    private MDependency decorateDependenciesWithBuilds(MDependency mDependency, Map<ArtifactId, BuildTypeId> artifactBuildMapping) {
        ArtifactId dependencyArtifactId = new ArtifactId(mDependency);
        MDependency decoratedDependency;
        if (artifactBuildMapping.containsKey(dependencyArtifactId)) {
            BuildTypeId buildTypeId = artifactBuildMapping.get(dependencyArtifactId);
            decoratedDependency = new MBuildTypeDependency(mDependency, buildTypeId.name, buildTypeId.buildTypeId, buildTypeId.projectName, buildTypeId.projectId);
        }
        else
            decoratedDependency = new MDependency(mDependency);

        decoratedDependency.getDependencies().clear();
        for (MDependency subDependency : mDependency.getDependencies())
            decoratedDependency.getDependencies().add(decorateDependenciesWithBuilds(subDependency, artifactBuildMapping));
        return decoratedDependency;
    }

    private void extractArtifactModuleMapping(MModule mModule, Map<ArtifactId, BuildTypeId> artifactBuildMapping, SBuildType buildType) {
        artifactBuildMapping.put(new ArtifactId(mModule), new BuildTypeId(buildType));
        for (MModule subModule: mModule.getSubModules())
            extractArtifactModuleMapping(subModule, artifactBuildMapping, buildType);
    }

    private class RequestCache {
        private Map<String, MavenDependenciesResult> resultsCache = newHashMap();
        
        public MavenDependenciesResult getBuildDependencies(SBuildType buildType) {
            if (resultsCache.containsKey(buildType.getBuildTypeId()))
                return resultsCache.get(buildType.getBuildTypeId());
            else {
                MavenDependenciesResult buildDependencies = mavenDependenciesAnalyzer.getBuildDependencies(buildType);
                resultsCache.put(buildType.getBuildTypeId(), buildDependencies);
                return buildDependencies;
            }
        }
        
        public void put(SBuildType buildType, MavenDependenciesResult buildDependencies) {
            resultsCache.put(buildType.getBuildTypeId(), buildDependencies);
        }
    }
    
    private class ArtifactId {
        private String groupId;
        private String artifactId;

        public ArtifactId(MArtifact mArtifact) {
            this.groupId = mArtifact.getGroupId();
            this.artifactId = mArtifact.getArtifactId();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ArtifactId that = (ArtifactId) o;

            if (artifactId != null ? !artifactId.equals(that.artifactId) : that.artifactId != null) return false;
            if (groupId != null ? !groupId.equals(that.groupId) : that.groupId != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = groupId != null ? groupId.hashCode() : 0;
            result = 31 * result + (artifactId != null ? artifactId.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return String.format("%s:%s", groupId, artifactId);
        }
    }
    
    private class BuildTypeId {
        private String name;
        private String projectName;
        private String buildTypeId;
        private String projectId;

        public BuildTypeId(SBuildType buildType) {
            this.name = buildType.getName();
            this.buildTypeId = buildType.getBuildTypeId();
            this.projectId = buildType.getProjectId();
            this.projectName = buildType.getProjectName();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BuildTypeId buildTypeId = (BuildTypeId) o;

            if (this.buildTypeId != null ? !this.buildTypeId.equals(buildTypeId.buildTypeId) : buildTypeId.buildTypeId != null)
                return false;
            if (name != null ? !name.equals(buildTypeId.name) : buildTypeId.name != null) return false;
            if (projectId != null ? !projectId.equals(buildTypeId.projectId) : buildTypeId.projectId != null) return false;
            if (projectName != null ? !projectName.equals(buildTypeId.projectName) : buildTypeId.projectName != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (projectName != null ? projectName.hashCode() : 0);
            result = 31 * result + (buildTypeId != null ? buildTypeId.hashCode() : 0);
            result = 31 * result + (projectId != null ? projectId.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return String.format("%s:%s (%s:%s)", projectName, name, projectId, buildTypeId);
        }

    }
}
