package com.wixpress.ci.teamcity.mavenAnalyzer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wixpress.ci.teamcity.domain.*;
import com.wixpress.ci.teamcity.maven.MavenBooter;
import com.wixpress.ci.teamcity.maven.MavenProjectDependenciesAnalyzer;
import com.wixpress.ci.teamcity.mavenAnalyzer.dao.BuildTypeDependenciesStorage;
import com.wixpress.ci.teamcity.mavenAnalyzer.dao.DependenciesDao;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.vcs.VcsException;
import jetbrains.buildServer.vcs.VcsRootInstance;
import org.junit.Test;
import org.mockito.Matchers;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author yoav
 * @since 2/19/12
 */
public class TestMavenBuildDependenciesAnalyzerAnalyzeDependencies {
    
    MavenBooter mavenBooter = mock(MavenBooter.class);
    DependenciesDao dependenciesDao = mock(DependenciesDao.class);
    MavenProjectDependenciesAnalyzer mavenProjectDependenciesAnalyzer = mock(MavenProjectDependenciesAnalyzer.class);
    CollectDependenciesExecutor executor = mock(CollectDependenciesExecutor.class);
    CollectDependenciesRunner runner = mock(CollectDependenciesRunner.class);
    VcsRootInstance vcsRootInstance = mock(VcsRootInstance.class);
    SBuildType buildType = mock(SBuildType.class);
    MavenBuildTypeDependenciesAnalyzer analyzer =
            new MavenBuildTypeDependenciesAnalyzer(mavenBooter, dependenciesDao, mavenProjectDependenciesAnalyzer, executor);

    
    @Test
    public void dependenciesNeverRun() throws IOException {
        when(executor.getRunner(buildType)).thenReturn(null);
        when(dependenciesDao.load(buildType)).thenReturn(null);

        MavenDependenciesResult result = analyzer.analyzeDependencies(buildType);

        assertThat(result.getResultType(), is(ResultType.runningAsync));
        verify(executor).purgeOldRuns();
        verify(executor).execute(Matchers.<CollectDependenciesRunner>any());
    }

    @Test
    public void analyzeDependenciesCurrent() throws IOException, VcsException {
        when(executor.getRunner(buildType)).thenReturn(null);
        when(dependenciesDao.load(buildType)).thenReturn(serializedDefaultModuleStorage());
        when(buildType.getVcsRootInstances()).thenReturn(ImmutableList.of(vcsRootInstance));
        when(vcsRootInstance.getName()).thenReturn("vcs1");
        when(vcsRootInstance.getCurrentRevision()).thenReturn("1.2");

        MavenDependenciesResult result = analyzer.analyzeDependencies(buildType);

        assertThat(result.getResultType(), is(ResultType.current));
        verify(executor, never()).execute(Matchers.<CollectDependenciesRunner>any());
    }
    
    @Test
    public void analyzeDependenciesNewRevision() throws IOException, VcsException {
        when(executor.getRunner(buildType)).thenReturn(null);
        when(dependenciesDao.load(buildType)).thenReturn(serializedDefaultModuleStorage());
        when(buildType.getVcsRootInstances()).thenReturn(ImmutableList.of(vcsRootInstance));
        when(vcsRootInstance.getName()).thenReturn("vcs1");
        when(vcsRootInstance.getCurrentRevision()).thenReturn("1.3");

        MavenDependenciesResult result = analyzer.analyzeDependencies(buildType);

        assertThat(result.getResultType(), is(ResultType.runningAsync));
        verify(executor).execute(Matchers.<CollectDependenciesRunner>any());
    }
    
    @Test
    public void getBuildDependenciesCurrent() throws IOException, VcsException {
        when(executor.getRunner(buildType)).thenReturn(null);
        when(dependenciesDao.load(buildType)).thenReturn(serializedDefaultModuleStorage());
        when(buildType.getVcsRootInstances()).thenReturn(ImmutableList.of(vcsRootInstance));
        when(vcsRootInstance.getName()).thenReturn("vcs1");
        when(vcsRootInstance.getCurrentRevision()).thenReturn("1.2");

        MavenDependenciesResult result = analyzer.getBuildDependencies(buildType, true);

        assertThat(result.getResultType(), is(ResultType.current));
        verify(executor, never()).execute(Matchers.<CollectDependenciesRunner>any());
    }

    @Test
    public void getBuildDependenciesNewRevisionWithCheck() throws IOException, VcsException {
        when(executor.getRunner(buildType)).thenReturn(null);
        when(dependenciesDao.load(buildType)).thenReturn(serializedDefaultModuleStorage());
        when(buildType.getVcsRootInstances()).thenReturn(ImmutableList.of(vcsRootInstance));
        when(vcsRootInstance.getName()).thenReturn("vcs1");
        when(vcsRootInstance.getCurrentRevision()).thenReturn("1.3");

        MavenDependenciesResult result = analyzer.getBuildDependencies(buildType, true);

        assertThat(result.getResultType(), is(ResultType.needsRefresh));
        verify(executor, never()).execute(Matchers.<CollectDependenciesRunner>any());
    }

    @Test
    public void getBuildDependenciesNewRevisionWithoutCheck() throws IOException, VcsException {
        when(executor.getRunner(buildType)).thenReturn(null);
        when(dependenciesDao.load(buildType)).thenReturn(serializedDefaultModuleStorage());
        when(buildType.getVcsRootInstances()).thenReturn(ImmutableList.of(vcsRootInstance));
        when(vcsRootInstance.getName()).thenReturn("vcs1");
        when(vcsRootInstance.getCurrentRevision()).thenReturn("1.3");

        MavenDependenciesResult result = analyzer.getBuildDependencies(buildType, false);

        assertThat(result.getResultType(), is(ResultType.current));
        verify(executor, never()).execute(Matchers.<CollectDependenciesRunner>any());
    }

    @Test
    public void analyzeDependenciesRunning() throws IOException {
        when(executor.getRunner(buildType)).thenReturn(runner);
        when(runner.isCompleted()).thenReturn(false);

        MavenDependenciesResult result = analyzer.analyzeDependencies(buildType);

        assertThat(result.getResultType(), is(ResultType.runningAsync));
        verify(executor, never()).execute(Matchers.<CollectDependenciesRunner>any());
        verify(buildType, never()).getCustomDataStorage(anyString());
    }
    
    @Test
    public void analyzeDependenciesRunningCompleted() throws IOException, VcsException {
        when(executor.getRunner(buildType)).thenReturn(runner);
        when(runner.isCompleted()).thenReturn(true);
        when(dependenciesDao.load(buildType)).thenReturn(serializedDefaultModuleStorage());
        when(buildType.getVcsRootInstances()).thenReturn(ImmutableList.of(vcsRootInstance));
        when(vcsRootInstance.getName()).thenReturn("vcs1");
        when(vcsRootInstance.getCurrentRevision()).thenReturn("1.2");

        MavenDependenciesResult result = analyzer.analyzeDependencies(buildType);

        assertThat(result.getResultType(), is(ResultType.current));
        verify(executor, never()).execute(Matchers.<CollectDependenciesRunner>any());
    }

    @Test
    public void analyzeDependenciesRunFailed() throws IOException, VcsException {
        when(executor.getRunner(buildType)).thenReturn(null);
        when(runner.isCompleted()).thenReturn(true);
        when(dependenciesDao.load(buildType)).thenReturn(serializedModuleStorageWithError());

        MavenDependenciesResult result = analyzer.analyzeDependencies(buildType);

        assertThat(result.getResultType(), is(ResultType.exception));
        verify(executor, never()).execute(Matchers.<CollectDependenciesRunner>any());
    }

    @Test
    public void forceAnalyzeDependenciesRunFailed() throws IOException, VcsException {
        when(executor.getRunner(buildType)).thenReturn(null);
        when(runner.isCompleted()).thenReturn(true);
        when(dependenciesDao.load(buildType)).thenReturn(serializedModuleStorageWithError());

        MavenDependenciesResult result = analyzer.forceAnalyzeDependencies(buildType);
        assertThat(result.getResultType(), is(ResultType.runningAsync));

        verify(executor).execute(Matchers.<CollectDependenciesRunner>any());
    }
    
    @Test
    public void getProgressRunning() {
        when(buildType.getBuildTypeId()).thenReturn("id32");
        when(executor.getRunner(buildType.getBuildTypeId())).thenReturn(runner);
        CollectProgress collectProgress = mock(CollectProgress.class);
        when(runner.getProgress(0, "id32")).thenReturn(collectProgress);
        
        CollectProgress collectProgressRes = analyzer.getProgress(buildType.getBuildTypeId(), 0);
        assertThat(collectProgressRes, sameInstance(collectProgress));
    }

    @Test
    public void getProgressNotRunning() {
        when(executor.getRunner(buildType)).thenReturn(null);
        when(buildType.getBuildTypeId()).thenReturn("id32");

        CollectProgress collectProgressRes = analyzer.getProgress(buildType.getBuildTypeId(), 0);
        assertThat(collectProgressRes.isRunFound(), is(false));
    }

    private BuildTypeDependenciesStorage serializedDefaultModuleStorage() throws IOException {
        MModule module = new MModule("module", "artifact", "1.2");
        Map<String, String> rev = ImmutableMap.of("vcs1", "1.2");
        return new BuildTypeDependenciesStorage(module, rev);
    }

    private BuildTypeDependenciesStorage serializedModuleStorageWithError() throws IOException {
        return new BuildTypeDependenciesStorage(ImmutableList.of(new LogMessage("failed running", LogMessageType.error)));
    }
}
