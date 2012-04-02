package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.google.common.collect.ImmutableList;
import com.wixpress.ci.teamcity.domain.BuildTypeId;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author yoav
 * @since 2/26/12
 */
public class TopologicalSorterTest extends AlgorithmTest {
    
    @Test
    public void dependsOnProperlyOrdered() {
        TestNode n4 = new TestNode(buildTypeId4);
        TestNode n3 = new TestNode(buildTypeId3, n4);
        TestNode n2 = new TestNode(buildTypeId2, n3);
        TestNode n1 = new TestNode(buildTypeId1, n2);
        
        List<TestNode> sorted = new TopologicalSorter<TestNode>(ImmutableList.of(n1, n2, n3, n4)).sort();

        assertTrue(sorted.indexOf(n1) < sorted.indexOf(n2));
        assertTrue(sorted.indexOf(n2) < sorted.indexOf(n3));
        assertTrue(sorted.indexOf(n3) < sorted.indexOf(n4));
    }
    
    @Test
    public void missingDependency() {
        TestNode n4 = new TestNode(buildTypeId4);
        TestNode n3 = new TestNode(buildTypeId3, n4);
        TestNode n2 = new TestNode(buildTypeId2, n3);
        TestNode n1 = new TestNode(buildTypeId1, n2);

        List<TestNode> sorted = new TopologicalSorter<TestNode>(ImmutableList.of(n1, n2, n3)).sort();

        assertTrue(sorted.indexOf(n1) < sorted.indexOf(n2));
        assertTrue(sorted.indexOf(n2) < sorted.indexOf(n3));
        assertTrue(sorted.indexOf(n4) == -1);
    }
    
}
