package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.wixpress.ci.teamcity.domain.BuildTypeId;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * @author yoav
 * @since 2/26/12
 */
public class TopologicalSorterTest {
    
    BuildTypeId buildTypeId1 = new BuildTypeId("n1", "p1", "n1", "p1");
    BuildTypeId buildTypeId2 = new BuildTypeId("n2", "p1", "n2", "p1");
    BuildTypeId buildTypeId3 = new BuildTypeId("n3", "p1", "n3", "p1");
    BuildTypeId buildTypeId4 = new BuildTypeId("n4", "p1", "n4", "p1");
    
    @Test
    public void dependsOnProperlyOrdered() {
        Map<BuildTypeId, Set<BuildTypeId>> graph = Maps.newHashMap();
        graph.put(buildTypeId1, ImmutableSet.of(buildTypeId2));
        graph.put(buildTypeId2, ImmutableSet.of(buildTypeId3));
        graph.put(buildTypeId3, ImmutableSet.of(buildTypeId4));
        graph.put(buildTypeId4, ImmutableSet.<BuildTypeId>of());

        List<BuildTypeId> sorted = new TopologicalSorter(graph).sort();

        assertTrue(sorted.indexOf(buildTypeId1) < sorted.indexOf(buildTypeId2));
        assertTrue(sorted.indexOf(buildTypeId2) < sorted.indexOf(buildTypeId3));
        assertTrue(sorted.indexOf(buildTypeId3) < sorted.indexOf(buildTypeId4));
    }
    
    @Test
    public void missingDependency() {
        Map<BuildTypeId, Set<BuildTypeId>> graph = Maps.newHashMap();
        graph.put(buildTypeId1, ImmutableSet.of(buildTypeId2));
        graph.put(buildTypeId2, ImmutableSet.of(buildTypeId3));
        graph.put(buildTypeId3, ImmutableSet.of(buildTypeId4));

        List<BuildTypeId> sorted = new TopologicalSorter(graph).sort();

        assertTrue(sorted.indexOf(buildTypeId1) < sorted.indexOf(buildTypeId2));
        assertTrue(sorted.indexOf(buildTypeId2) < sorted.indexOf(buildTypeId3));
        assertTrue(sorted.indexOf(buildTypeId4) == -1);
    }
}
