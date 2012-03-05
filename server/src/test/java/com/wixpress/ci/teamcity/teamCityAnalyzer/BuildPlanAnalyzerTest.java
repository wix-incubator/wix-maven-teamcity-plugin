package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.google.common.collect.ImmutableList;
import com.wixpress.ci.teamcity.dependenciesTab.DependenciesTabConfigModel;
import com.wixpress.ci.teamcity.domain.*;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.vcs.SVcsModification;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static com.wixpress.ci.teamcity.maven.Matchers.IsMBuildPlanItem;
import static com.wixpress.ci.teamcity.teamCityAnalyzer.Builders.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author yoav
 * @since 2/26/12
 */
public class BuildPlanAnalyzerTest {

    private ProjectManager projectManager = mock(ProjectManager.class);
    private DependenciesTabConfigModel configModel = mock(DependenciesTabConfigModel.class);
    private DependenciesTabConfig config = mock(DependenciesTabConfig.class);
    
    BuildPlanAnalyzer sorter = new BuildPlanAnalyzer(projectManager, configModel);
    Date d1 = new Date(2011, 1, 1, 1, 1);
    Date d2 = new Date(2011, 1, 1, 1, 2);
    Date d3 = new Date(2011, 1, 1, 1, 3);
    Date d4 = new Date(2011, 1, 1, 1, 4);

    @Before
    public void initMocks() {
        when(configModel.getConfig()).thenReturn(config);
    }
    
    @Test
    public void testSortBuildTypes() {
        MModule build3moduleC3 = MModule("com.wixpress", "C3", "3")
                .withDependency(dependency("a", "a", dependency("b", "b")))
                .build();
        MModule build3moduleC4 = MModule("com.wixpress", "C4", "4")
                .withDependency(build3moduleC3)
                .withDependency(dependency("b", "b"))
                .build();
        MModule build3Root = MModule("com.wixpress", "C5", "5")
                .withModule(build3moduleC3)
                .withModule(build3moduleC4)
                .build();
        mockBuild("root", false, d3);
        mockBuild("a", false, d2);
        mockBuild("b", false, d1);

        List<MBuildPlanItem> sorted = sorter.sortBuildTypes(build3Root, buildTypeId("root"));

        assertTrue(indexOf(sorted, buildPlanItem("root")) < indexOf(sorted, buildPlanItem("a")));
        assertTrue(indexOf(sorted, buildPlanItem("a")) < indexOf(sorted, buildPlanItem("b")));
    }

    @Test
    public void testSortBuildTypesWithMultipleDependenciesOfSameBuildType() {
        MModule build3moduleC3 = MModule("com.wixpress", "C3", "3")
                .withDependency(dependency("a", "bt1", dependency("b", "bt1", dependency("c", "bt2"))))
                .build();
        MModule build3moduleC4 = MModule("com.wixpress", "C4", "4")
                .withDependency(build3moduleC3)
                .withDependency(dependency("b", "bt1"))
                .build();
        MModule build3Root = MModule("com.wixpress", "C5", "5")
                .withModule(build3moduleC3)
                .withModule(build3moduleC4)
                .build();
        mockBuild("root", false, d3);
        mockBuild("bt1", false, d2);
        mockBuild("bt2", false, d1);

        List<MBuildPlanItem> sorted = sorter.sortBuildTypes(build3Root, buildTypeId("root"));

        assertTrue(indexOf(sorted, buildPlanItem("root")) < indexOf(sorted, buildPlanItem("bt1")));
        assertTrue(indexOf(sorted, buildPlanItem("bt1")) < indexOf(sorted, buildPlanItem("bt2")));
    }
    
    @Test
    public void ignoreTestProvidedScopes() {
        MModule module = MModule("com.wixpress", "C3", "3")
                .withDependency(dependency("a", "bt1", 
                        dependency("b", "bt1", 
                                dependency("c", "bt2", "provided"), 
                                dependency("d", "bt3", "test")),
                        dependency("d", "bt3", "test")))
                .build();
        mockBuild("root", false, d3);
        mockBuild("bt1", false, d2);
        mockBuild("bt2", false, d1);
        mockBuild("bt3", false, d1);

        List<MBuildPlanItem> sorted = sorter.sortBuildTypes(module, buildTypeId("root"));
        assertThat(sorted, not(hasItem(buildPlanItem("bt3"))));
        assertThat(sorted, not(hasItem(buildPlanItem("bt2"))));
        assertThat(sorted, hasItem(buildPlanItem("bt1")));
    }

    @Test
    public void dependentBuildNevenBuilt() {
        MModule module = MModule("com.wixpress", "C3", "3")
                .withDependency(dependency("a", "bt1",
                        dependency("b", "bt1",
                                dependency("c", "bt2"),
                                dependency("d", "bt3")),
                        dependency("d", "bt3")))
                .build();
        mockBuild("root", false, d3);
        mockBuild("bt1", false, d2);
        mockBuild("bt2", false, null);
        mockBuild("bt3", false, d1);

        List<MBuildPlanItem> sorted = sorter.sortBuildTypes(module, buildTypeId("root"));

        assertThat(sorted, hasItem(IsMBuildPlanItem(is(buildTypeId("root")), true, containsString("[proj-bt1:name-bt1] require building"))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(buildTypeId("bt1")), true, containsString("[proj-bt2:name-bt2] require building"))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(buildTypeId("bt2")), true, containsString("No successful build found"))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(buildTypeId("bt3")), false, any(String.class))));
    }

    @Test
    public void dependentBuildHasPendingChanges() {
        MModule module = MModule("com.wixpress", "C3", "3")
                .withDependency(dependency("a", "bt1",
                        dependency("b", "bt1",
                                dependency("c", "bt2"),
                                dependency("d", "bt3")),
                        dependency("d", "bt3")))
                .build();
        mockBuild("root", false, d3);
        mockBuild("bt1", false, d2);
        mockBuild("bt2", true, d1);
        mockBuild("bt3", false, d1);

        List<MBuildPlanItem> sorted = sorter.sortBuildTypes(module, buildTypeId("root"));

        assertThat(sorted, hasItem(IsMBuildPlanItem(is(buildTypeId("root")), true, containsString("[proj-bt1:name-bt1] require building"))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(buildTypeId("bt1")), true, containsString("[proj-bt2:name-bt2] require building"))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(buildTypeId("bt2")), true, containsString("Has pending changes"))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(buildTypeId("bt3")), false, any(String.class))));
    }

    @Test
    public void dependentBuildHasIgnoredPendingChanges() {
        when(config.getCommitsToIgnore()).thenReturn(ImmutableList.of("pending*"));
        MModule module = MModule("com.wixpress", "C3", "3")
                .withDependency(dependency("a", "bt1",
                        dependency("b", "bt1",
                                dependency("c", "bt2"),
                                dependency("d", "bt3")),
                        dependency("d", "bt3")))
                .build();
        mockBuild("root", false, d3);
        mockBuild("bt1", false, d2);
        mockBuild("bt2", true, d1);
        mockBuild("bt3", false, d1);

        List<MBuildPlanItem> sorted = sorter.sortBuildTypes(module, buildTypeId("root"));

        assertThat(sorted, hasItem(IsMBuildPlanItem(is(buildTypeId("root")), false, any(String.class))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(buildTypeId("bt1")), false, any(String.class))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(buildTypeId("bt2")), false, any(String.class))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(buildTypeId("bt3")), false, any(String.class))));
    }

    @Test
    public void dependentBuildIsNewer() {
        MModule module = MModule("com.wixpress", "C3", "3")
                .withDependency(dependency("a", "bt1",
                        dependency("b", "bt1",
                                dependency("c", "bt2"),
                                dependency("d", "bt3")),
                        dependency("d", "bt3")))
                .build();
        mockBuild("root", false, d4);
        mockBuild("bt1", false, d2);
        mockBuild("bt2", false, d3);
        mockBuild("bt3", false, d1);

        List<MBuildPlanItem> sorted = sorter.sortBuildTypes(module, buildTypeId("root"));

        assertThat(sorted, hasItem(IsMBuildPlanItem(is(buildTypeId("root")), true, containsString("[proj-bt1:name-bt1] require building"))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(buildTypeId("bt1")), true, containsString("[proj-bt2:name-bt2] last build is newer"))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(buildTypeId("bt2")), false, any(String.class))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(buildTypeId("bt3")), false, any(String.class))));
    }


    private void mockBuild(String buildTypeId, boolean hasPendingChanges, Date lastSuccessFullBuild) {
        SBuildType buildType = mock(SBuildType.class);
        SVcsModification vcsModification = mock(SVcsModification.class);
        when(vcsModification.getDescription()).thenReturn("pending change");
        SFinishedBuild finishedBuild = mock(SFinishedBuild.class);
        when(projectManager.findBuildTypeById(buildTypeId)).thenReturn(buildType);
        when(buildType.getPendingChanges()).thenReturn(hasPendingChanges? ImmutableList.of(vcsModification):ImmutableList.<SVcsModification>of());
        if (lastSuccessFullBuild != null) {
            when(buildType.getLastChangesSuccessfullyFinished()).thenReturn(finishedBuild);
            when(finishedBuild.getClientStartDate()).thenReturn(lastSuccessFullBuild);
            when(finishedBuild.getFinishDate()).thenReturn(lastSuccessFullBuild);
        }
        else 
            when(buildType.getLastChangesSuccessfullyFinished()).thenReturn(null);
    }

    private MBuildTypeDependency dependency(String id, String buildTypeId, MBuildTypeDependency ... childDep) {
        MBuildTypeDependency buildTypeDependency = new MBuildTypeDependency("group" + id, "artifact" + id, "1", "compile", false, "name-" + buildTypeId, "proj-" + buildTypeId, buildTypeId, "p" + buildTypeId);
        for (MBuildTypeDependency child: childDep)
            buildTypeDependency.getDependencies().add(child);
        return buildTypeDependency;
    }
    
    private MBuildTypeDependency dependency(String id, String buildTypeId, String scope, MBuildTypeDependency ... childDep) {
        MBuildTypeDependency buildTypeDependency = new MBuildTypeDependency("group" + id, "artifact" + id, "1", scope, false, "name-" + buildTypeId, "proj-" + buildTypeId, buildTypeId, "p" + buildTypeId);
        for (MBuildTypeDependency child: childDep)
            buildTypeDependency.getDependencies().add(child);
        return buildTypeDependency;
    }

    private Matcher<MBuildPlanItem> buildPlanItem(String buildTypeId) {
        return IsMBuildPlanItem(is(buildTypeId(buildTypeId)), false, any(String.class));
    }
    
    private BuildTypeId buildTypeId(String buildTypeId) {
        return new BuildTypeId("name-" + buildTypeId, "proj-" + buildTypeId, buildTypeId, "p" + buildTypeId);
    }
    
    public <T> int indexOf(Iterable<T> iterable, Matcher<T> matcher) {
        int pos = 0;
        for (T t: iterable) {
            if (matcher.matches(t)) 
                return pos;
            pos++;
        }
        return -1;
    }
}
