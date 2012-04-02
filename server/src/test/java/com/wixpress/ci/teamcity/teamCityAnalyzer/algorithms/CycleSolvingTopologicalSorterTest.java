package com.wixpress.ci.teamcity.teamCityAnalyzer.algorithms;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author yoav
 * @since 4/2/12
 */
public class CycleSolvingTopologicalSorterTest extends AlgorithmTest {

    @Test
    public void dependsOnProperlyOrdered() {
        TestNodeWithRemove n4 = new TestNodeWithRemove(buildTypeId4);
        TestNodeWithRemove n3 = new TestNodeWithRemove(buildTypeId3, n4);
        TestNodeWithRemove n2 = new TestNodeWithRemove(buildTypeId2, n3);
        TestNodeWithRemove n1 = new TestNodeWithRemove(buildTypeId1, n2);

        List<TestNodeWithRemove> sorted = new CycleSolvingTopologicalSorter<TestNodeWithRemove>(ImmutableList.of(n1, n2, n3, n4)).sort();

        assertTrue(sorted.indexOf(n1) < sorted.indexOf(n2));
        assertTrue(sorted.indexOf(n2) < sorted.indexOf(n3));
        assertTrue(sorted.indexOf(n3) < sorted.indexOf(n4));
    }

    @Test
    public void missingDependency() {
        TestNodeWithRemove n4 = new TestNodeWithRemove(buildTypeId4);
        TestNodeWithRemove n3 = new TestNodeWithRemove(buildTypeId3, n4);
        TestNodeWithRemove n2 = new TestNodeWithRemove(buildTypeId2, n3);
        TestNodeWithRemove n1 = new TestNodeWithRemove(buildTypeId1, n2);

        List<TestNodeWithRemove> sorted = new CycleSolvingTopologicalSorter<TestNodeWithRemove>(ImmutableList.of(n1, n2, n3)).sort();

        assertTrue(sorted.indexOf(n1) < sorted.indexOf(n2));
        assertTrue(sorted.indexOf(n2) < sorted.indexOf(n3));
        assertTrue(sorted.indexOf(n4) == -1);
    }

    @Test
    public void sortGraphWithCycle() {
        TestNodeWithRemove n4 = new TestNodeWithRemove(buildTypeId4);
        TestNodeWithRemove n3 = new TestNodeWithRemove(buildTypeId3, n4);
        TestNodeWithRemove n2 = new TestNodeWithRemove(buildTypeId2, n3);
        TestNodeWithRemove n1 = new TestNodeWithRemove(buildTypeId1, n2);
        n4.setChildren(n2);

        List<TestNodeWithRemove> sorted = new CycleSolvingTopologicalSorter<TestNodeWithRemove>(ImmutableList.of(n1, n2, n3, n4)).sort();

        assertTrue(sorted.size() == 4);
    }
}
