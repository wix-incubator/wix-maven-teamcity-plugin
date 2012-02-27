package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.wixpress.ci.teamcity.domain.*;
import com.wixpress.ci.teamcity.mavenAnalyzer.MavenBuildTypeDependenciesAnalyzer;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

/**
* @author yoav
* @since 2/26/12
*/
class BuildTypeDependenciesDecorator {

    private MavenBuildTypeDependenciesAnalyzer mavenBuildAnalyzer;
    private ProjectManager projectManager;

    public BuildTypeDependenciesDecorator(MavenBuildTypeDependenciesAnalyzer mavenBuildAnalyzer, ProjectManager projectManager) {
        this.mavenBuildAnalyzer = mavenBuildAnalyzer;
        this.projectManager = projectManager;
    }


    public MavenDependenciesResult decorateWithBuildTypesAnalysis(MavenDependenciesResult mavenResult, SBuildType buildType) {
        RequestCache requestCache = new RequestCache();
        requestCache.put(buildType, mavenResult);
        Map<ArtifactId, BuildTypeId> artifactBuildMapping = readArtifactModuleMapping(requestCache);
        return new MavenDependenciesResult(mavenResult.getResultType(), decorateDependenciesWithBuilds(mavenResult.getModule(), artifactBuildMapping), mavenResult.getFullTrace());
    }

    private Map<ArtifactId, BuildTypeId> readArtifactModuleMapping(RequestCache requestCache) {
        Map<ArtifactId, BuildTypeId> artifactBuildMapping = newHashMap();
        for (SBuildType buildType: projectManager.getActiveBuildTypes()) {
            MavenDependenciesResult buildDependencies = requestCache.getBuildDependencies(buildType, false);
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

        List<MDependency> dependencies = newArrayList();
        boolean hasChildBuildTypeDependency = false;
        for (MDependency subDependency : mDependency.getDependencies()) {
            MDependency childDependency = decorateDependenciesWithBuilds(subDependency, artifactBuildMapping);
            hasChildBuildTypeDependency = hasChildBuildTypeDependency || childDependency instanceof MBuildTypeDependency;
            dependencies.add(childDependency);
        }

        MDependency decoratedDependency;
        if (artifactBuildMapping.containsKey(dependencyArtifactId)) {
            BuildTypeId buildTypeId = artifactBuildMapping.get(dependencyArtifactId);
            decoratedDependency = new MBuildTypeDependency(mDependency, buildTypeId);
        }
        else if (hasChildBuildTypeDependency) {
            decoratedDependency = new MBuildTypeDependency(mDependency, new BuildTypeId("unknown", "unknown", mDependency.getGroupId(), mDependency.getArtifactId(), false));
        }
        else
            decoratedDependency = new MDependency(mDependency);

        decoratedDependency.setDependencies(dependencies);
        return decoratedDependency;
    }

    private void extractArtifactModuleMapping(MModule mModule, Map<ArtifactId, BuildTypeId> artifactBuildMapping, SBuildType buildType) {
        artifactBuildMapping.put(new ArtifactId(mModule), new BuildTypeId(buildType));
        for (MModule subModule: mModule.getSubModules())
            extractArtifactModuleMapping(subModule, artifactBuildMapping, buildType);
    }

    private class RequestCache {
        private Map<String, MavenDependenciesResult> resultsCache = newHashMap();

        public MavenDependenciesResult getBuildDependencies(SBuildType buildType, boolean checkForNewerRevision) {
            if (resultsCache.containsKey(buildType.getBuildTypeId()))
                return resultsCache.get(buildType.getBuildTypeId());
            else {
                MavenDependenciesResult buildDependencies = mavenBuildAnalyzer.getBuildDependencies(buildType, checkForNewerRevision);
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

}
