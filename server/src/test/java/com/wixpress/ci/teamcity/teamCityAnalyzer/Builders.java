package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.wixpress.ci.teamcity.domain.MDependency;
import com.wixpress.ci.teamcity.domain.MModule;

/**
 * @author yoav
 * @since 2/26/12
 */
public class Builders {

    public static MModuleBuilder MModule(String groupId, String artifactId, String version) {
        return new MModuleBuilder(groupId, artifactId, version);
    }

    public static class MModuleBuilder {
        MModule mModule;
        public MModuleBuilder(String groupId, String artifactId, String version) {
            this.mModule = new MModule(groupId, artifactId, version);
            mModule.setDependencyTree(new MDependency(mModule.getGroupId(), mModule.getArtifactId(), mModule.getVersion(), "compile", false));
        }

        public MModuleBuilder withModule(MModule module) {
            mModule.getSubModules().add(module);
            return this;
        }

        public MModuleBuilder withDependency(MModule module) {
            mModule.getDependencyTree().getDependencies().add(module.getDependencyTree());
            return this;
        }

        public MModuleBuilder withDependency(MDependency dependency) {
            mModule.getDependencyTree().getDependencies().add(dependency);
            return this;
        }

        public MModule build() {
            return mModule;
        }
    }

}
