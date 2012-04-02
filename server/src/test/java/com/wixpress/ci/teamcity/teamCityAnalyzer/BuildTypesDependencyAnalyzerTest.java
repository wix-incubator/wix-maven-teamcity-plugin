package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.google.common.collect.ImmutableList;
import com.wixpress.ci.teamcity.domain.*;
import com.wixpress.ci.teamcity.mavenAnalyzer.MavenBuildTypeDependenciesAnalyzer;
import com.wixpress.ci.teamcity.mavenAnalyzer.dao.DependenciesDao;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author yoav
 * @since 2/27/12
 */
public class BuildTypesDependencyAnalyzerTest {

    private MavenBuildTypeDependenciesAnalyzer mavenBuildAnalyzer = mock(MavenBuildTypeDependenciesAnalyzer.class);
    private BuildTypeDependenciesDecorator dependenciesDecorator = mock(BuildTypeDependenciesDecorator.class);
    private BuildPlanAnalyzer dependenciesSorter = mock(BuildPlanAnalyzer.class);
    private SBuildType buildType = mock(SBuildType.class);
    private BuildTypeDependenciesExtractor dependenciesExtractor = mock(BuildTypeDependenciesExtractor.class);
    private DependenciesDao dependenciesDao = mock(DependenciesDao.class);

    private BuildTypesDependencyAnalyzer analyzer = new BuildTypesDependencyAnalyzer(mavenBuildAnalyzer, dependenciesDecorator, dependenciesSorter, dependenciesExtractor, dependenciesDao);

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

}
