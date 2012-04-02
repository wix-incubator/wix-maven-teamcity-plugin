package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.wixpress.ci.teamcity.domain.*;
import com.wixpress.ci.teamcity.teamCityAnalyzer.entity.BuildTypeDependencies;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author yoav
 * @since 4/2/12
 */
public class BuildTypeDependenciesExtractor {

    public BuildTypeDependencies extract(MModule module, BuildTypeId buildTypeId) {
        DependencyExtractorVisitor visitor = new DependencyExtractorVisitor(buildTypeId);
        module.accept(visitor);
        return visitor.getBuildTypedependencies();
    }
    
    private class DependencyExtractorVisitor implements MArtifactVisitor {

        final BuildTypeDependencies dependencies;

        public DependencyExtractorVisitor(BuildTypeId buildTypeId) {
            this.dependencies = new BuildTypeDependencies(buildTypeId);
        }


        public boolean visitEnter(MArtifact mArtifact) {
            if (mArtifact instanceof MBuildTypeDependency) {
                MBuildTypeDependency buildTypeDependency = (MBuildTypeDependency)mArtifact;
                BuildTypeId buildTypeId = buildTypeDependency.getBuildTypeId();
                if (!("test".equals(buildTypeDependency.getScope()) || "provided".equals(buildTypeDependency.getScope()))) {
                    dependencies.getDependencies().add(buildTypeId);
                    return true;
                }
                return false;
            }
            return true;
        }

        public boolean visitLeave(MArtifact mArtifact) {
            return true;
        }

        public BuildTypeDependencies getBuildTypedependencies() {
            return dependencies;
        }
    }
    
}
