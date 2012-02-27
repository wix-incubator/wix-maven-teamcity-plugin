package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.google.common.collect.ImmutableList;
import com.wixpress.ci.teamcity.domain.*;
import com.wixpress.ci.teamcity.mavenAnalyzer.MavenBuildTypeDependenciesAnalyzer;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.vcs.SVcsModification;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.Date;

import static com.wixpress.ci.teamcity.maven.Matchers.IsMBuildPlanItem;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author yoav
 * @since 2/27/12
 */
public class BuildTypesDependencyAnalyzerTest {

    private MavenBuildTypeDependenciesAnalyzer mavenBuildAnalyzer = mock(MavenBuildTypeDependenciesAnalyzer.class);
    private ProjectManager projectManager = mock(ProjectManager.class);
    private BuildTypeDependenciesDecorator dependenciesDecorator = mock(BuildTypeDependenciesDecorator.class);
    private BuildTypeDependenciesSorter dependenciesSorter = mock(BuildTypeDependenciesSorter.class);
    private SBuildType buildType = mock(SBuildType.class);

    private MavenDependenciesResult mavenDependenciesResultCurrent = new MavenDependenciesResult(ResultType.current);
    
    
    private BuildTypesDependencyAnalyzer analyzer = new BuildTypesDependencyAnalyzer(mavenBuildAnalyzer, projectManager, dependenciesDecorator, dependenciesSorter);

    @Test
    public void testGetBuildDependencies_Delegate() throws Exception {
        when(mavenBuildAnalyzer.getBuildDependencies(buildType, true)).thenReturn(new MavenDependenciesResult(ResultType.runningAsync));

        assertThat(analyzer.getBuildDependencies(buildType).getResultType(), is(ResultType.runningAsync));
    }

    @Test
    public void testAnalyzeDependencies_Delegate() throws Exception {
        when(mavenBuildAnalyzer.analyzeDependencies(buildType)).thenReturn(new MavenDependenciesResult(ResultType.runningAsync));

        assertThat(analyzer.analyzeDependencies(buildType).getResultType(), is(ResultType.runningAsync));
    }

    @Test
    public void testForceAnalyzeDependencies_Delegate() throws Exception {
        when(mavenBuildAnalyzer.forceAnalyzeDependencies(buildType)).thenReturn(new MavenDependenciesResult(ResultType.runningAsync));

        assertThat(analyzer.forceAnalyzeDependencies(buildType).getResultType(), is(ResultType.runningAsync));
    }

    @Test
    public void testGetProgress_Delegate() throws Exception {
        when(mavenBuildAnalyzer.getProgress("bt2", 0)).thenReturn(new CollectProgress("bt2"));

        assertThat(analyzer.getProgress("bt2", 0).getBuildTypeId(), is("bt2"));
    }
    
    @Test
    public void testBuildPlan_NoBuildNeeded() throws Exception {
        when(mavenBuildAnalyzer.getBuildDependencies(buildType, true)).thenReturn(mavenDependenciesResultCurrent);
        when(dependenciesDecorator.decorateWithBuildTypesAnalysis(Matchers.<MavenDependenciesResult>anyObject(), Matchers.<SBuildType>anyObject()))
                .thenReturn(mavenDependenciesResultCurrent);
        BuildTypeId a = buildTypeId("a", false, new Date(2011, 1, 1, 1, 3));
        BuildTypeId b = buildTypeId("b", false, new Date(2011, 1, 1, 1, 2));
        BuildTypeId c = buildTypeId("c", false, new Date(2011, 1, 1, 1, 1));
        when(dependenciesSorter.sortBuildTypes(Matchers.<MModule>anyObject(), Matchers.<BuildTypeId>anyObject())).thenReturn(ImmutableList.of(a, b, c));

        BuildDependenciesResult result = analyzer.getBuildDependencies(buildType);

        assertThat(result.getResultType(), is(ResultType.current));
        assertThat(result.getBuildPlan(), hasItem(IsMBuildPlanItem(is(a), false, any(String.class))));
        assertThat(result.getBuildPlan(), hasItem(IsMBuildPlanItem(is(b), false, any(String.class))));
        assertThat(result.getBuildPlan(), hasItem(IsMBuildPlanItem(is(c), false, any(String.class))));
    }
    
    @Test
    public void testBuildPlan_BuildNeeded_BneverBuilt() throws Exception {
        when(mavenBuildAnalyzer.getBuildDependencies(buildType, true)).thenReturn(mavenDependenciesResultCurrent);
        when(dependenciesDecorator.decorateWithBuildTypesAnalysis(Matchers.<MavenDependenciesResult>anyObject(), Matchers.<SBuildType>anyObject()))
                .thenReturn(mavenDependenciesResultCurrent);
        BuildTypeId a = buildTypeId("a", false, new Date(2011, 1, 1, 1, 3));
        BuildTypeId b = buildTypeId("b", false, null);
        BuildTypeId c = buildTypeId("c", false, new Date(2011, 1, 1, 1, 1));
        when(dependenciesSorter.sortBuildTypes(Matchers.<MModule>anyObject(), Matchers.<BuildTypeId>anyObject())).thenReturn(ImmutableList.of(a, b, c));

        BuildDependenciesResult result = analyzer.getBuildDependencies(buildType);

        assertThat(result.getResultType(), is(ResultType.current));
        assertThat(result.getBuildPlan(), hasItem(IsMBuildPlanItem(is(a), true, containsString("Dependency [projb:nameb] require building"))));
        assertThat(result.getBuildPlan(), hasItem(IsMBuildPlanItem(is(b), true, containsString("No successful build found"))));
        assertThat(result.getBuildPlan(), hasItem(IsMBuildPlanItem(is(c), false, any(String.class))));
    }

    @Test
    public void testBuildPlan_BuildNeeded_BhasPendingChanges() throws Exception {
        when(mavenBuildAnalyzer.getBuildDependencies(buildType, true)).thenReturn(mavenDependenciesResultCurrent);
        when(dependenciesDecorator.decorateWithBuildTypesAnalysis(Matchers.<MavenDependenciesResult>anyObject(), Matchers.<SBuildType>anyObject()))
                .thenReturn(mavenDependenciesResultCurrent);
        BuildTypeId a = buildTypeId("a", false, new Date(2011, 1, 1, 1, 3));
        BuildTypeId b = buildTypeId("b", true, new Date(2011, 1, 1, 1, 2));
        BuildTypeId c = buildTypeId("c", false, new Date(2011, 1, 1, 1, 1));
        when(dependenciesSorter.sortBuildTypes(Matchers.<MModule>anyObject(), Matchers.<BuildTypeId>anyObject())).thenReturn(ImmutableList.of(a, b, c));

        BuildDependenciesResult result = analyzer.getBuildDependencies(buildType);

        assertThat(result.getResultType(), is(ResultType.current));
        assertThat(result.getBuildPlan(), hasItem(IsMBuildPlanItem(is(a), true, containsString("Dependency [projb:nameb] require building"))));
        assertThat(result.getBuildPlan(), hasItem(IsMBuildPlanItem(is(b), true, containsString("Has pending changes"))));
        assertThat(result.getBuildPlan(), hasItem(IsMBuildPlanItem(is(c), false, any(String.class))));
    }

    @Test
    public void testBuildPlan_BuildNeeded_BnewerThenA() throws Exception {
        when(mavenBuildAnalyzer.getBuildDependencies(buildType, true)).thenReturn(mavenDependenciesResultCurrent);
        when(dependenciesDecorator.decorateWithBuildTypesAnalysis(Matchers.<MavenDependenciesResult>anyObject(), Matchers.<SBuildType>anyObject()))
                .thenReturn(mavenDependenciesResultCurrent);
        BuildTypeId a = buildTypeId("a", false, new Date(2011, 1, 1, 1, 3));
        BuildTypeId b = buildTypeId("b", false, new Date(2011, 1, 1, 1, 4));
        BuildTypeId c = buildTypeId("c", false, new Date(2011, 1, 1, 1, 1));
        when(dependenciesSorter.sortBuildTypes(Matchers.<MModule>anyObject(), Matchers.<BuildTypeId>anyObject())).thenReturn(ImmutableList.of(a, b, c));

        BuildDependenciesResult result = analyzer.getBuildDependencies(buildType);

        assertThat(result.getResultType(), is(ResultType.current));
        assertThat(result.getBuildPlan(), hasItem(IsMBuildPlanItem(is(a), true, containsString("Dependency [projb:nameb] last build is newer"))));
        assertThat(result.getBuildPlan(), hasItem(IsMBuildPlanItem(is(b), false, any(String.class))));
        assertThat(result.getBuildPlan(), hasItem(IsMBuildPlanItem(is(c), false, any(String.class))));
    }

    private BuildTypeId buildTypeId(String id, boolean hasPendingChanges, Date lastSuccessFullBuild) {
        SBuildType buildType = mock(SBuildType.class);
        SVcsModification vcsModification = mock(SVcsModification.class);
        SFinishedBuild finishedBuild = mock(SFinishedBuild.class);
        when(projectManager.findBuildTypeById(id)).thenReturn(buildType);
        when(buildType.getPendingChanges()).thenReturn(hasPendingChanges?ImmutableList.of(vcsModification):ImmutableList.<SVcsModification>of());
        if (lastSuccessFullBuild != null) {
            when(buildType.getLastChangesSuccessfullyFinished()).thenReturn(finishedBuild);
            when(finishedBuild.getClientStartDate()).thenReturn(lastSuccessFullBuild);
        }
        else 
            when(buildType.getLastChangesSuccessfullyFinished()).thenReturn(null);
        
        return new BuildTypeId("name" + id, "proj" + id, id, "p" + id);
    }
    

}
