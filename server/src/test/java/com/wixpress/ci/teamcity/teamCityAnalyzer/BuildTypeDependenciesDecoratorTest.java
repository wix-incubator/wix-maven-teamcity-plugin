package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.google.common.collect.ImmutableList;
import com.wixpress.ci.teamcity.domain.*;
import com.wixpress.ci.teamcity.mavenAnalyzer.MavenBuildTypeDependenciesAnalyzer;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import org.junit.Test;

import static com.wixpress.ci.teamcity.maven.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author yoav
 * @since 2/22/12
 */
public class BuildTypeDependenciesDecoratorTest {

    ProjectManager projectManager = mock(ProjectManager.class);
    MavenBuildTypeDependenciesAnalyzer mavenBuildAnalyzer = mock(MavenBuildTypeDependenciesAnalyzer.class);
    BuildTypeDependenciesDecorator analyzer = new BuildTypeDependenciesDecorator(mavenBuildAnalyzer, projectManager);

    SBuildType build1 = BuildType("proj1", "build1", "p1", "b1");
    SBuildType build2 = BuildType("proj2", "build2", "p2", "b2");
    SBuildType build3 = BuildType("proj3", "build3", "p3", "b3");

    @Test
    public void decorateDependencies() {
        MModule build1Root = MModule("com.wixpress", "A", "1").build();
        MModule build2Root = MModule("com.wixpress", "B2", "2").withDependency(build1Root).build();
        MModule build3moduleC3 = MModule("com.wixpress", "C3", "3").withDependency(build2Root).build();
        MModule build3moduleC4 = MModule("com.wixpress", "C4", "4").withDependency(build3moduleC3).build();
        MModule build3Root = MModule("com.wixpress", "C5", "5").withModule(build3moduleC3).withModule(build3moduleC4).build();
        when(projectManager.getActiveBuildTypes()).thenReturn(ImmutableList.of(build1, build2, build3));
        when(mavenBuildAnalyzer.getBuildDependencies(eq(build1), anyBoolean())).thenReturn(new MavenDependenciesResult(build1Root));
        when(mavenBuildAnalyzer.getBuildDependencies(eq(build2), anyBoolean())).thenReturn(new MavenDependenciesResult(build2Root));
        MavenDependenciesResult mavenDependenciesResult = new MavenDependenciesResult(build3Root);
        when(mavenBuildAnalyzer.getBuildDependencies(eq(build3), anyBoolean())).thenReturn(mavenDependenciesResult);

        BuildDependenciesResult result = analyzer.decorateWithBuildTypesAnalysis(mavenDependenciesResult, build3);
        
        assertThat(result.getModule(), IsModule("com.wixpress", "C5", "5"));
        assertThat(result.getModule(), new ArtifactTreeMatcher<IArtifact>()
                .match(IsModule("com.wixpress", "C3", "3")));
        assertThat(result.getModule(), new ArtifactTreeMatcher<IArtifact>()
                .get(IsArtifact("com.wixpress", "C3", "3"))
                .match(IsMBuildTypeDependency("com.wixpress", "B2", "2", "proj2", "build2", "p2", "b2")));
        assertThat(result.getModule(), new ArtifactTreeMatcher<IArtifact>()
                .get(IsArtifact("com.wixpress", "C3", "3"))
                .get(IsArtifact("com.wixpress", "B2", "2"))
                .match(IsMBuildTypeDependency("com.wixpress", "A", "1", "proj1", "build1", "p1", "b1")));
        assertThat(result.getModule(), new ArtifactTreeMatcher<IArtifact>()
                .match(IsModule("com.wixpress", "C4", "4")));
        assertThat(result.getModule(), new ArtifactTreeMatcher<IArtifact>()
                .get(IsArtifact("com.wixpress", "C4", "4"))
                .match(IsMDependency("com.wixpress", "C3", "3")));
        assertThat(result.getModule(), new ArtifactTreeMatcher<IArtifact>()
                .get(IsArtifact("com.wixpress", "C4", "4"))
                .get(IsArtifact("com.wixpress", "C3", "3"))
                .match(IsMBuildTypeDependency("com.wixpress", "B2", "2", "proj2", "build2", "p2", "b2")));
    }

    private SBuildType BuildType(String projectName, String name, String projectId, String buildTypeId) {
        SBuildType buildType = mock(SBuildType.class);
        when(buildType.getBuildTypeId()).thenReturn(buildTypeId);
        when(buildType.getName()).thenReturn(name);
        when(buildType.getProjectName()).thenReturn(projectName);
        when(buildType.getProjectId()).thenReturn(projectId);
        return buildType;

    }

    private MModuleBuilder MModule(String groupId, String artifactId, String version) {
        return new MModuleBuilder(groupId, artifactId, version);
    }

    private class MModuleBuilder {
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
