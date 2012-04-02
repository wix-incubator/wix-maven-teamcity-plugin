package com.wixpress.ci.teamcity.teamCityAnalyzer.algorithms;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.wixpress.ci.teamcity.domain.BuildTypeId;

import java.util.Collection;

/**
 * @author yoav
 * @since 4/2/12
 */
public abstract class AlgorithmTest {


    protected BuildTypeId buildTypeId1 = new BuildTypeId("n1", "p1", "n1", "p1");
    protected BuildTypeId buildTypeId2 = new BuildTypeId("n2", "p1", "n2", "p1");
    protected BuildTypeId buildTypeId3 = new BuildTypeId("n3", "p1", "n3", "p1");
    protected BuildTypeId buildTypeId4 = new BuildTypeId("n4", "p1", "n4", "p1");

    protected class TestVertex<T extends TestVertex<T>> implements Vertex<T> {
        BuildTypeId buildTypeId;
        Collection<T> children;

        protected TestVertex(BuildTypeId buildTypeId, T ... children) {
            this.buildTypeId = buildTypeId;
            this.children = ImmutableList.copyOf(children);
        }

        public Iterable<T> getChildren() {
            return children;
        }

        public void setChildren(T ... children) {
            this.children = ImmutableList.copyOf(children);
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            T testNode = (T) o;

            return !(buildTypeId != null ? !buildTypeId.equals(testNode.buildTypeId) : testNode.buildTypeId != null);

        }

        @Override
        public int hashCode() {
            return buildTypeId != null ? buildTypeId.hashCode() : 0;
        }
    }

    protected class TestNode extends TestVertex<TestNode>{

        protected TestNode(BuildTypeId buildTypeId, TestNode... children) {
            super(buildTypeId, children);
        }
    }

    protected class TestNodeWithRemove extends TestVertex<TestNodeWithRemove> implements IgnoreEdgesCapable<TestNodeWithRemove> {


        protected TestNodeWithRemove(BuildTypeId buildTypeId, TestNodeWithRemove... children) {
            super(buildTypeId, children);
        }

        public void ignoreEdgeTo(final TestNodeWithRemove child) {
            this.children = Collections2.filter(this.children, new Predicate<TestNodeWithRemove>() {
                public boolean apply(TestNodeWithRemove input) {
                    return !input.equals(child);
                }
            });
        }
    }
}
