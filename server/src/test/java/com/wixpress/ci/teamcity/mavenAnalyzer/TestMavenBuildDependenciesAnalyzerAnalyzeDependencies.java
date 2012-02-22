package com.wixpress.ci.teamcity.mavenAnalyzer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wixpress.ci.teamcity.domain.*;
import com.wixpress.ci.teamcity.maven.MavenBooter;
import com.wixpress.ci.teamcity.maven.MavenProjectDependenciesAnalyzer;
import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.vcs.VcsException;
import jetbrains.buildServer.vcs.VcsRootInstance;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.mockito.Matchers;

import java.io.IOException;
import java.util.Map;

import static com.wixpress.ci.teamcity.mavenAnalyzer.TeamCityBuildMavenDependenciesAnalyzer.ModuleStorage;
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
    ObjectMapper objectMapper = new ObjectMapper();
    MavenProjectDependenciesAnalyzer mavenProjectDependenciesAnalyzer = mock(MavenProjectDependenciesAnalyzer.class);
    CollectDependenciesExecutor executor = mock(CollectDependenciesExecutor.class);
    CollectDependenciesRunner runner = mock(CollectDependenciesRunner.class);
    VcsRootInstance vcsRootInstance = mock(VcsRootInstance.class);
    SBuildType buildType = mock(SBuildType.class);
    CustomDataStorage customDataStorage = mock(CustomDataStorage.class);
    TeamCityBuildMavenDependenciesAnalyzer analyzer =
            new TeamCityBuildMavenDependenciesAnalyzer(mavenBooter, objectMapper, mavenProjectDependenciesAnalyzer, executor);

    
    @Test
    public void dependenciesNeverRun() {
        when(executor.getRunner(buildType)).thenReturn(null);
        when(buildType.getCustomDataStorage(TeamCityBuildMavenDependenciesAnalyzer.DEPENDENCIES_STORAGE)).thenReturn(customDataStorage);
        when(customDataStorage.getValue(TeamCityBuildMavenDependenciesAnalyzer.BUILD_DEPENDENCIES)).thenReturn(null);
        
        MavenDependenciesResult result = analyzer.analyzeDependencies(buildType);

        assertThat(result.getResultType(), is(ResultType.runningAsync));
        verify(executor).purgeOldRuns();
        verify(executor).execute(Matchers.<CollectDependenciesRunner>any());
    }

    @Test
    public void analyzeDependenciesCurrent() throws IOException, VcsException {
        when(executor.getRunner(buildType)).thenReturn(null);
        when(buildType.getCustomDataStorage(TeamCityBuildMavenDependenciesAnalyzer.DEPENDENCIES_STORAGE)).thenReturn(customDataStorage);
        when(customDataStorage.getValue(TeamCityBuildMavenDependenciesAnalyzer.BUILD_DEPENDENCIES)).thenReturn(serializedDefaultModuleStorage());
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
        when(buildType.getCustomDataStorage(TeamCityBuildMavenDependenciesAnalyzer.DEPENDENCIES_STORAGE)).thenReturn(customDataStorage);
        when(customDataStorage.getValue(TeamCityBuildMavenDependenciesAnalyzer.BUILD_DEPENDENCIES)).thenReturn(serializedDefaultModuleStorage());
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
        when(buildType.getCustomDataStorage(TeamCityBuildMavenDependenciesAnalyzer.DEPENDENCIES_STORAGE)).thenReturn(customDataStorage);
        when(customDataStorage.getValue(TeamCityBuildMavenDependenciesAnalyzer.BUILD_DEPENDENCIES)).thenReturn(serializedDefaultModuleStorage());
        when(buildType.getVcsRootInstances()).thenReturn(ImmutableList.of(vcsRootInstance));
        when(vcsRootInstance.getName()).thenReturn("vcs1");
        when(vcsRootInstance.getCurrentRevision()).thenReturn("1.2");

        MavenDependenciesResult result = analyzer.getBuildDependencies(buildType);

        assertThat(result.getResultType(), is(ResultType.current));
        verify(executor, never()).execute(Matchers.<CollectDependenciesRunner>any());
    }

    @Test
    public void getBuildDependenciesNewRevision() throws IOException, VcsException {
        when(executor.getRunner(buildType)).thenReturn(null);
        when(buildType.getCustomDataStorage(TeamCityBuildMavenDependenciesAnalyzer.DEPENDENCIES_STORAGE)).thenReturn(customDataStorage);
        when(customDataStorage.getValue(TeamCityBuildMavenDependenciesAnalyzer.BUILD_DEPENDENCIES)).thenReturn(serializedDefaultModuleStorage());
        when(buildType.getVcsRootInstances()).thenReturn(ImmutableList.of(vcsRootInstance));
        when(vcsRootInstance.getName()).thenReturn("vcs1");
        when(vcsRootInstance.getCurrentRevision()).thenReturn("1.3");

        MavenDependenciesResult result = analyzer.getBuildDependencies(buildType);

        assertThat(result.getResultType(), is(ResultType.needsRefresh));
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
        when(buildType.getCustomDataStorage(TeamCityBuildMavenDependenciesAnalyzer.DEPENDENCIES_STORAGE)).thenReturn(customDataStorage);
        when(customDataStorage.getValue(TeamCityBuildMavenDependenciesAnalyzer.BUILD_DEPENDENCIES)).thenReturn(serializedDefaultModuleStorage());
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
        when(buildType.getCustomDataStorage(TeamCityBuildMavenDependenciesAnalyzer.DEPENDENCIES_STORAGE)).thenReturn(customDataStorage);
        when(customDataStorage.getValue(TeamCityBuildMavenDependenciesAnalyzer.BUILD_DEPENDENCIES)).thenReturn(serializedModuleStorageWithError());

        MavenDependenciesResult result = analyzer.analyzeDependencies(buildType);

        assertThat(result.getResultType(), is(ResultType.exception));
        verify(executor, never()).execute(Matchers.<CollectDependenciesRunner>any());
    }

    @Test
    public void forceAnalyzeDependenciesRunFailed() throws IOException, VcsException {
        when(executor.getRunner(buildType)).thenReturn(null);
        when(runner.isCompleted()).thenReturn(true);
        when(buildType.getCustomDataStorage(TeamCityBuildMavenDependenciesAnalyzer.DEPENDENCIES_STORAGE)).thenReturn(customDataStorage);
        when(customDataStorage.getValue(TeamCityBuildMavenDependenciesAnalyzer.BUILD_DEPENDENCIES)).thenReturn(serializedModuleStorageWithError());

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

    private String serializedDefaultModuleStorage() throws IOException {
        MModule module = new MModule("module", "artifact", "1.2");
        Map<String, String> rev = ImmutableMap.of("vcs1", "1.2");
        ModuleStorage moduleStorage = new ModuleStorage(module, rev);
        return objectMapper.writeValueAsString(moduleStorage);
    }

    private String serializedModuleStorageWithError() throws IOException {
        ModuleStorage moduleStorage = new ModuleStorage(ImmutableList.of(new LogMessage("failed running", LogMessageType.error)));
        return objectMapper.writeValueAsString(moduleStorage);
    }
}
