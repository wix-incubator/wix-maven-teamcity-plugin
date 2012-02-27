package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.wixpress.ci.teamcity.domain.BuildTypeId;
import org.junit.Test;

import java.util.Collection;
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
    
    private class TestNode implements TopologicalSorter.Node<TestNode> {
        BuildTypeId buildTypeId;
        Iterable<TestNode> children;

        private TestNode(BuildTypeId buildTypeId, TestNode ... children) {
            this.buildTypeId = buildTypeId;
            this.children = ImmutableList.copyOf(children);
        }

        public Iterable<TestNode> getChildren() {
            return children;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestNode testNode = (TestNode) o;

            if (buildTypeId != null ? !buildTypeId.equals(testNode.buildTypeId) : testNode.buildTypeId != null)
                return false;
            if (children != null ? !children.equals(testNode.children) : testNode.children != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = buildTypeId != null ? buildTypeId.hashCode() : 0;
            result = 31 * result + (children != null ? children.hashCode() : 0);
            return result;
        }
    }
}
