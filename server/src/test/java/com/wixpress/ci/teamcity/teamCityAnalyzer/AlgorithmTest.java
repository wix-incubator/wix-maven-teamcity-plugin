package com.wixpress.ci.teamcity.teamCityAnalyzer;

import com.google.common.collect.ImmutableList;
import com.wixpress.ci.teamcity.domain.BuildTypeId;

/**
 * @author yoav
 * @since 4/2/12
 */
public abstract class AlgorithmTest {


    protected BuildTypeId buildTypeId1 = new BuildTypeId("n1", "p1", "n1", "p1");
    protected BuildTypeId buildTypeId2 = new BuildTypeId("n2", "p1", "n2", "p1");
    protected BuildTypeId buildTypeId3 = new BuildTypeId("n3", "p1", "n3", "p1");
    protected BuildTypeId buildTypeId4 = new BuildTypeId("n4", "p1", "n4", "p1");

    protected class TestNode implements Vertex<TestNode> {
        BuildTypeId buildTypeId;
        Iterable<TestNode> children;

        protected TestNode(BuildTypeId buildTypeId, TestNode ... children) {
            this.buildTypeId = buildTypeId;
            this.children = ImmutableList.copyOf(children);
        }

        public Iterable<TestNode> getChildren() {
            return children;
        }

        public void setChildren(TestNode ... children) {
            this.children = ImmutableList.copyOf(children);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestNode testNode = (TestNode) o;

            if (buildTypeId != null ? !buildTypeId.equals(testNode.buildTypeId) : testNode.buildTypeId != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = buildTypeId != null ? buildTypeId.hashCode() : 0;
            return result;
        }
    }
}
