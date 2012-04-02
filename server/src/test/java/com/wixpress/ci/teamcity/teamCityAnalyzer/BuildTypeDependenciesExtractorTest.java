package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.wixpress.ci.teamcity.domain.BuildTypeId;
import com.wixpress.ci.teamcity.domain.MBuildTypeDependency;
import com.wixpress.ci.teamcity.domain.MModule;
import com.wixpress.ci.teamcity.teamCityAnalyzer.entity.BuildTypeDependencies;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author yoav
 * @since 4/2/12
 */
public class BuildTypeDependenciesExtractorTest {

    BuildTypeDependenciesExtractor extractor = new BuildTypeDependenciesExtractor();

    @Test
    public void testExtractDependenciesFromModule() {
        MModule module = Builders.MModule("com.wixpress", "C3", "3")
                .withDependency(
                        dependency("a", BuildTypeId("a"), "compile",
                                dependency("b", BuildTypeId("b"), "compile"),
                                dependency("c", BuildTypeId("c"), "compile")))
                .withDependency(
                        dependency("d", BuildTypeId("d"), "compile",
                                dependency("e", BuildTypeId("e"), "compile")))
                .build();
        BuildTypeId buildTypeId = new BuildTypeId("C5", "com.wixpress", "tb1", "p1");

        BuildTypeDependencies res = extractor.extract(module, buildTypeId);
        assertThat(res.getBuildTypeId(), is(buildTypeId));
        assertThat(res.getDependencies(),
                hasItems(
                        BuildTypeId("a"),
                        BuildTypeId("b"),
                        BuildTypeId("c"),
                        BuildTypeId("d"),
                        BuildTypeId("e")));

    }

    @Test
    public void ignoreProvidedScope() {
        MModule module = Builders.MModule("com.wixpress", "C3", "3")
                .withDependency(
                        dependency("a", BuildTypeId("a"), "compile",
                                dependency("b", BuildTypeId("b"), "compile"),
                                dependency("c", BuildTypeId("c"), "compile")))
                .withDependency(
                        dependency("d", BuildTypeId("d"), "provided",
                                dependency("e", BuildTypeId("e"), "compile")))
                .build();
        BuildTypeId buildTypeId = new BuildTypeId("C5", "com.wixpress", "tb1", "p1");

        BuildTypeDependencies res = extractor.extract(module, buildTypeId);
        assertThat(res.getBuildTypeId(), is(buildTypeId));
        assertThat(res.getDependencies(),
                hasItems(
                        BuildTypeId("a"),
                        BuildTypeId("b"),
                        BuildTypeId("c")));
        assertThat(res.getDependencies(), not(hasItem(BuildTypeId("d"))));
        assertThat(res.getDependencies(), not(hasItem(BuildTypeId("e"))));

    }

    @Test
    public void ignoreTestScope() {
        MModule module = Builders.MModule("com.wixpress", "C3", "3")
                .withDependency(
                        dependency("a", BuildTypeId("a"), "compile",
                                dependency("b", BuildTypeId("b"), "compile"),
                                dependency("c", BuildTypeId("c"), "compile")))
                .withDependency(
                        dependency("d", BuildTypeId("d"), "test",
                                dependency("e", BuildTypeId("e"), "compile")))
                .build();
        BuildTypeId buildTypeId = new BuildTypeId("C5", "com.wixpress", "tb1", "p1");

        BuildTypeDependencies res = extractor.extract(module, buildTypeId);
        assertThat(res.getBuildTypeId(), is(buildTypeId));
        assertThat(res.getDependencies(),
                hasItems(
                        BuildTypeId("a"),
                        BuildTypeId("b"),
                        BuildTypeId("c")));
        assertThat(res.getDependencies(), not(hasItem(BuildTypeId("d"))));
        assertThat(res.getDependencies(), not(hasItem(BuildTypeId("e"))));

    }

    @Test
    public void testExtractDependenciesFromMultipleModules() {
        MModule build3moduleC3 = Builders.MModule("com.wixpress", "C3", "3")
                .withDependency(
                        dependency("a", BuildTypeId("a"), "compile",
                                dependency("b", BuildTypeId("b"), "compile")))
                .build();
        MModule build3moduleC4 = Builders.MModule("com.wixpress", "C4", "4")
                .withDependency(
                        build3moduleC3)
                .withDependency(
                        dependency("b", BuildTypeId("b"), "compile"))
                .build();
        MModule build3Root = Builders.MModule("com.wixpress", "C5", "5")
                .withModule(
                        build3moduleC3)
                .withModule(
                        build3moduleC4)
                .build();
        BuildTypeId buildTypeId = new BuildTypeId("C5", "com.wixpress", "tb1", "p1");

        BuildTypeDependencies res = extractor.extract(build3Root, buildTypeId);
        assertThat(res.getBuildTypeId(), is(buildTypeId));
        assertThat(res.getDependencies(), hasItems(
                BuildTypeId("a"),
                BuildTypeId("b")));

    }

    private MBuildTypeDependency dependency(String id, BuildTypeId buildTypeId, String scope, MBuildTypeDependency ... childDep) {
        MBuildTypeDependency buildTypeDependency = new MBuildTypeDependency("group" + id, "artifact" + id, "1", scope, false,
                buildTypeId.getName(), buildTypeId.getProjectName(), buildTypeId.getBuildTypeId(), buildTypeId.getProjectId());
        for (MBuildTypeDependency child: childDep)
            buildTypeDependency.getDependencies().add(child);
        return buildTypeDependency;
    }

    private BuildTypeId BuildTypeId(String key) {
        return new BuildTypeId("name-" + key, "proj-" + key, key, "p" + key);
    }

}
