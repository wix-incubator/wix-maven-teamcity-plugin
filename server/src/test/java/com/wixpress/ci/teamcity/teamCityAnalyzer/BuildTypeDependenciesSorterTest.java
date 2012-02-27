package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.wixpress.ci.teamcity.domain.BuildTypeId;
import com.wixpress.ci.teamcity.domain.MBuildTypeDependency;
import com.wixpress.ci.teamcity.domain.MModule;
import org.junit.Test;

import java.util.List;

import static com.wixpress.ci.teamcity.teamCityAnalyzer.Builders.*;
import static org.junit.Assert.assertTrue;

/**
 * @author yoav
 * @since 2/26/12
 */
public class BuildTypeDependenciesSorterTest {

    BuildTypeDependenciesSorter sorter = new BuildTypeDependenciesSorter();

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

        List<BuildTypeId> sorted = sorter.sortBuildTypes(build3Root, buildTypeId("root"));
        assertTrue(sorted.indexOf(buildTypeId("root")) < sorted.indexOf(buildTypeId("a")));
        assertTrue(sorted.indexOf(buildTypeId("a")) < sorted.indexOf(buildTypeId("b")));
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

        List<BuildTypeId> sorted = sorter.sortBuildTypes(build3Root, buildTypeId("root"));
        assertTrue(sorted.indexOf(buildTypeId("root")) < sorted.indexOf(buildTypeId("bt1")));
        assertTrue(sorted.indexOf(buildTypeId("bt1")) < sorted.indexOf(buildTypeId("bt2")));
    }

    private MBuildTypeDependency dependency(String id, String buildTypeId, MBuildTypeDependency ... childDep) {
        MBuildTypeDependency buildTypeDependency = new MBuildTypeDependency("group" + id, "artifact" + id, "1", "compile", false, "name" + buildTypeId, "proj" + buildTypeId, buildTypeId, "p" + buildTypeId);
        for (MBuildTypeDependency child: childDep)
            buildTypeDependency.getDependencies().add(child);
        return buildTypeDependency;
    }
    
    private BuildTypeId buildTypeId(String id) {
        return new BuildTypeId("name" + id, "proj" + id, id, "p" + id);
    }
}
