package com.wixpress.ci.teamcity.teamCityAnalyzer.algorithms;

import com.google.common.collect.ImmutableList;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author yoav
 * @since 4/2/12
 */
public class TarganSCCsAlgorithmTest extends AlgorithmTest {

    @Test
    public void oneNode() {
        TestNode n1 = new TestNode(buildTypeId1);

        Set<Set<TestNode>> sccs = new TarganSCCsAlgorithm<TestNode>(ImmutableList.of(n1)).sccs();

        assertThat(sccs.size(), is(1));

    }

    @Test
    public void graph() {
        TestNode n4 = new TestNode(buildTypeId4);
        TestNode n3 = new TestNode(buildTypeId3, n4);
        TestNode n2 = new TestNode(buildTypeId2, n3);
        TestNode n1 = new TestNode(buildTypeId1, n2, n4);

        Set<Set<TestNode>> sccs = new TarganSCCsAlgorithm<TestNode>(ImmutableList.of(n1, n2, n3, n4)).sccs();

        assertThat(sccs.size(), is(4));

    }

    @Test
    public void OneCycle() {
        TestNode n4 = new TestNode(buildTypeId4);
        TestNode n3 = new TestNode(buildTypeId3, n4);
        TestNode n2 = new TestNode(buildTypeId2, n3);
        TestNode n1 = new TestNode(buildTypeId1, n2);
        n4.setChildren(n1);

        Set<Set<TestNode>> sccs = new TarganSCCsAlgorithm<TestNode>(ImmutableList.of(n1, n2, n3, n4)).sccs();

        assertThat(sccs.size(), is(1));
        assertThat(sccs.iterator().next().size(), is(4));

    }

    @Test
    public void missingDependency() {
        TestNode n4 = new TestNode(buildTypeId4);
        TestNode n3 = new TestNode(buildTypeId3, n4);
        TestNode n2 = new TestNode(buildTypeId2, n3);
        TestNode n1 = new TestNode(buildTypeId1, n2);
        n4.setChildren(n1);

        Set<Set<TestNode>> sccs = new TarganSCCsAlgorithm<TestNode>(ImmutableList.of(n1, n2, n3)).sccs();

        assertThat(sccs.size(), is(1));
        assertThat(sccs.iterator().next().size(), is(4));

    }

    @Test
    public void graphWithCycle() {
        TestNode n4 = new TestNode(buildTypeId4);
        TestNode n3 = new TestNode(buildTypeId3, n4);
        TestNode n2 = new TestNode(buildTypeId2, n3);
        TestNode n1 = new TestNode(buildTypeId1, n2);
        n4.setChildren(n2);

        Set<Set<TestNode>> sccs = new TarganSCCsAlgorithm<TestNode>(ImmutableList.of(n1, n2, n3, n4)).sccs();

        assertThat(sccs.size(), is(2));
        assertThat(sccs, hasSetItem(hasSetItems(n2, n3, n4)));
        assertThat(sccs, hasSetItem(hasSetItems(n1)));
    }
    
    @SuppressWarnings("unchecked")
    private <T> Matcher<Set<T>> hasSetItems(T ... items) {
        return (Matcher)hasItems(items);
    }
    
    @SuppressWarnings("unchecked")
    private <T> Matcher<Set<T>> hasSetItem(Matcher<T> matcher) {
        return (Matcher)hasItem(matcher);
    }
}
