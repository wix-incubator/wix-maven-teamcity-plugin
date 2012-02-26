package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.wixpress.ci.teamcity.domain.MBuildType;
import com.wixpress.ci.teamcity.domain.MBuildTypeDependency;
import com.wixpress.ci.teamcity.domain.MDependency;
import com.wixpress.ci.teamcity.domain.MModule;
import com.wixpress.ci.teamcity.maven.Matchers;
import jetbrains.buildServer.serverSide.SBuildType;
import org.junit.Test;

import static com.wixpress.ci.teamcity.maven.Matchers.IsMBuildType;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author yoav
 * @since 2/26/12
 */
public class BuildTypeDependencyTreeBuilderTest {
    
    BuildTypeDependencyTreeBuilder builder = new BuildTypeDependencyTreeBuilder();
    SBuildType buildType = mock(SBuildType.class);
             
    
    @Test
    public void buildModuleTree() {
        MModule module = new MModule("g1", "a1", "1");
        when(buildType.getName()).thenReturn("g1");
        when(buildType.getProjectName()).thenReturn("proj");
        when(buildType.getBuildTypeId()).thenReturn("bt1");
        when(buildType.getProjectId()).thenReturn("p1");
        MDependency root = new MDependency("g1", "a1", "1", "compile", false);
        MDependency child2 = new MBuildTypeDependency("g2", "a1", "1", "compile", false, "child2", "proj", "bt2", "p1");
        MDependency child3 = new MBuildTypeDependency("g3", "a1", "1", "compile", false, "child3", "proj", "bt3", "p1");
        MDependency child4 = new MDependency("g4", "a1", "1", "compile", false);
        MDependency child5 = new MDependency("g5", "a1", "1", "compile", false);
        MDependency child6 = new MDependency("g6", "a1", "1", "compile", false);
        module.setDependencyTree(root);
        root.getDependencies().add(child2);
        root.getDependencies().add(child4);
        child2.getDependencies().add(child3);
        child2.getDependencies().add(child5);
        child3.getDependencies().add(child6);
        

        MBuildType tree = builder.extractModuleBuildTypeDependenciesTree(module, buildType);

        assertThat(tree, IsMBuildType("g1", "proj", "bt1", "p1"));
        assertThat(tree, new Matchers.ArtifactTreeMatcher<MBuildType>()
                .match(IsMBuildType("child2", "proj", "bt2", "p1")));
        assertThat(tree, new Matchers.ArtifactTreeMatcher<MBuildType>()
                .get(IsMBuildType("child2", "proj", "bt2", "p1"))
                .match(IsMBuildType("child3", "proj", "bt3", "p1")));
        assertThat(tree.getDependencies().size(), is(1));
        assertThat(tree.getDependencies().get(0).getDependencies().size(), is(1));
    }
}
