package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.google.common.collect.ImmutableList;
import com.wixpress.ci.teamcity.dependenciesTab.ConfigModel;
import com.wixpress.ci.teamcity.domain.*;
import com.wixpress.ci.teamcity.mavenAnalyzer.dao.DependenciesDao;
import com.wixpress.ci.teamcity.teamCityAnalyzer.entity.BuildTypeDependencies;
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
    private ConfigModel configModel = mock(ConfigModel.class);
    private DependenciesTabConfig config = mock(DependenciesTabConfig.class);
    private DependenciesDao dependenciesDao = mock(DependenciesDao.class);

    BuildPlanAnalyzer sorter = new BuildPlanAnalyzer(projectManager, configModel, dependenciesDao);
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

        BuildTypeDependencies root = newBuildTypeDependencies("root", "a", "b");
        BuildTypeDependencies a = newBuildTypeDependencies("a");
        BuildTypeDependencies b = newBuildTypeDependencies("b");
        mockBuild(root, false, d3);
        mockBuild(a, false, d2);
        mockBuild(b, false, d1);

        List<MBuildPlanItem> sorted = sorter.getBuildPlan(root);

        assertTrue(indexOf(sorted, buildPlanItem("root")) < indexOf(sorted, buildPlanItem("a")));
        assertTrue(indexOf(sorted, buildPlanItem("a")) < indexOf(sorted, buildPlanItem("b")));
    }

    @Test
    public void testSortBuildTypesWithMultipleDependenciesOfSameBuildType() {
        BuildTypeDependencies root = newBuildTypeDependencies("root", "bt1", "bt2");
        BuildTypeDependencies bt1 = newBuildTypeDependencies("bt1", "bt2");
        BuildTypeDependencies bt2 = newBuildTypeDependencies("bt2");
        mockBuild(root, false, d3);
        mockBuild(bt1, false, d2);
        mockBuild(bt2, false, d1);

        List<MBuildPlanItem> sorted = sorter.getBuildPlan(root);
//
        assertTrue(indexOf(sorted, buildPlanItem("root")) < indexOf(sorted, buildPlanItem("bt1")));
        assertTrue(indexOf(sorted, buildPlanItem("bt1")) < indexOf(sorted, buildPlanItem("bt2")));
    }
    
    @Test
    public void dependentBuildNevenBuilt() {
        BuildTypeDependencies root = newBuildTypeDependencies("root", "bt1", "bt3");
        BuildTypeDependencies bt1 = newBuildTypeDependencies("bt1", "bt2", "bt3");
        BuildTypeDependencies bt2 = newBuildTypeDependencies("bt2");
        BuildTypeDependencies bt3 = newBuildTypeDependencies("bt3");
        mockBuild(root, false, d3);
        mockBuild(bt1, false, d2);
        mockBuild(bt2, false, null);
        mockBuild(bt3, false, d1);

        List<MBuildPlanItem> sorted = sorter.getBuildPlan(root);

        assertThat(sorted, hasItem(IsMBuildPlanItem(is(newBuildTypeId("root")), true, containsString("[proj-bt1:name-bt1] require building"))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(newBuildTypeId("bt1")), true, containsString("[proj-bt2:name-bt2] require building"))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(newBuildTypeId("bt2")), true, containsString("No successful build found"))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(newBuildTypeId("bt3")), false, any(String.class))));
    }

    @Test
    public void dependentBuildHasPendingChanges() {
        BuildTypeDependencies root = newBuildTypeDependencies("root", "bt1", "bt3");
        BuildTypeDependencies bt1 = newBuildTypeDependencies("bt1", "bt2", "bt3");
        BuildTypeDependencies bt2 = newBuildTypeDependencies("bt2");
        BuildTypeDependencies bt3 = newBuildTypeDependencies("bt3");
        mockBuild(root, false, d3);
        mockBuild(bt1, false, d2);
        mockBuild(bt2, true, d1);
        mockBuild(bt3, false, d1);

        List<MBuildPlanItem> sorted = sorter.getBuildPlan(root);

        assertThat(sorted, hasItem(IsMBuildPlanItem(is(newBuildTypeId("root")), true, containsString("[proj-bt1:name-bt1] require building"))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(newBuildTypeId("bt1")), true, containsString("[proj-bt2:name-bt2] require building"))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(newBuildTypeId("bt2")), true, containsString("Has pending changes"))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(newBuildTypeId("bt3")), false, any(String.class))));
    }

    @Test
    public void dependentBuildHasIgnoredPendingChanges() {
        when(config.getCommitsToIgnore()).thenReturn(ImmutableList.of("pending*"));
        BuildTypeDependencies root = newBuildTypeDependencies("root", "bt1", "bt3");
        BuildTypeDependencies bt1 = newBuildTypeDependencies("bt1", "bt2", "bt3");
        BuildTypeDependencies bt2 = newBuildTypeDependencies("bt2");
        BuildTypeDependencies bt3 = newBuildTypeDependencies("bt3");
        mockBuild(root, false, d3);
        mockBuild(bt1, false, d2);
        mockBuild(bt2, true, d1);
        mockBuild(bt3, false, d1);

        List<MBuildPlanItem> sorted = sorter.getBuildPlan(root);

        assertThat(sorted, hasItem(IsMBuildPlanItem(is(newBuildTypeId("root")), false, any(String.class))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(newBuildTypeId("bt1")), false, any(String.class))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(newBuildTypeId("bt2")), false, any(String.class))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(newBuildTypeId("bt3")), false, any(String.class))));
    }

    @Test
    public void dependentBuildIsNewer() {
        BuildTypeDependencies root = newBuildTypeDependencies("root", "bt1", "bt3");
        BuildTypeDependencies bt1 = newBuildTypeDependencies("bt1", "bt2", "bt3");
        BuildTypeDependencies bt2 = newBuildTypeDependencies("bt2");
        BuildTypeDependencies bt3 = newBuildTypeDependencies("bt3");
        mockBuild(root, false, d4);
        mockBuild(bt1, false, d2);
        mockBuild(bt2, false, d3);
        mockBuild(bt3, false, d1);

        List<MBuildPlanItem> sorted = sorter.getBuildPlan(root);

        assertThat(sorted, hasItem(IsMBuildPlanItem(is(newBuildTypeId("root")), true, containsString("[proj-bt1:name-bt1] require building"))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(newBuildTypeId("bt1")), true, containsString("[proj-bt2:name-bt2] last build is newer"))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(newBuildTypeId("bt2")), false, any(String.class))));
        assertThat(sorted, hasItem(IsMBuildPlanItem(is(newBuildTypeId("bt3")), false, any(String.class))));
    }

    private void mockBuild(BuildTypeDependencies buildTypeDependencies, boolean hasPendingChanges, Date lastSuccessFullBuild) {
        SBuildType buildType = mock(SBuildType.class);
        SVcsModification vcsModification = mock(SVcsModification.class);
        when(vcsModification.getDescription()).thenReturn("pending change");
        SFinishedBuild finishedBuild = mock(SFinishedBuild.class);
        when(projectManager.findBuildTypeById(buildTypeDependencies.getBuildTypeId().getBuildTypeId())).thenReturn(buildType);
        when(buildType.getPendingChanges()).thenReturn(hasPendingChanges? ImmutableList.of(vcsModification):ImmutableList.<SVcsModification>of());
        when(dependenciesDao.loadBuildDependencies(buildType)).thenReturn(buildTypeDependencies);
        if (lastSuccessFullBuild != null) {
            when(buildType.getLastChangesSuccessfullyFinished()).thenReturn(finishedBuild);
            when(finishedBuild.getClientStartDate()).thenReturn(lastSuccessFullBuild);
            when(finishedBuild.getFinishDate()).thenReturn(lastSuccessFullBuild);
        }
        else 
            when(buildType.getLastChangesSuccessfullyFinished()).thenReturn(null);
    }

    private Matcher<MBuildPlanItem> buildPlanItem(String buildTypeId) {
        return IsMBuildPlanItem(is(newBuildTypeId(buildTypeId)), false, any(String.class));
    }
    
    private BuildTypeId newBuildTypeId(String buildTypeId) {
        return new BuildTypeId("name-" + buildTypeId, "proj-" + buildTypeId, buildTypeId, "p" + buildTypeId);
    }
    
    private BuildTypeDependencies newBuildTypeDependencies(String buildTypeId, String ... dependencies) {
        BuildTypeDependencies buildTypeDependencies = new BuildTypeDependencies(newBuildTypeId(buildTypeId));
        for (String dependency:  dependencies)
            buildTypeDependencies.getDependencies().add(newBuildTypeId(dependency));
        return buildTypeDependencies;
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
