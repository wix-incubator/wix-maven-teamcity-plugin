package com.wixpress.ci.teamcity.teamCityAnalyzer;

import java.util.*;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

/**
 * algorithm to detect the cycles in a graph
 * @link http://en.wikipedia.org/wiki/Tarjan%E2%80%99s_strongly_connected_components_algorithm
 * @author yoav
 * @since 4/2/12
 */
public class TarganSCCsAlgorithm<VertexClass extends Vertex<VertexClass>> {

    private Collection<VertexClass> vertices;
    private int index = 0;
    private Deque<VertexClass> stack = newLinkedList();
    private Map<VertexClass, IndexOf> indexes = newHashMap();
    private Map<VertexClass, IndexOf> lowLinks = newHashMap();
    private Set<Set<VertexClass>> stronglyConnectedComponents = newHashSet();

    public TarganSCCsAlgorithm(Collection<VertexClass> vertices) {
        this.vertices = vertices;
        for (VertexClass vertex: vertices) {
            indexes.put(vertex, new IndexOf());
            lowLinks.put(vertex, new IndexOf());
        }
    }
    
    public Set<Set<VertexClass>> sccs() {
        for (VertexClass vertex: vertices)
            if (indexOf(vertex).isNotDefined())
                strongConnect(vertex);
        return stronglyConnectedComponents;
    }

    private void strongConnect(VertexClass vertex) {
        // Set the depth index for v to the smallest unused index
        indexOf(vertex).set(index);
        lowLinksOf(vertex).set(index);
        index = index + 1;
        stack.push(vertex);

        // Consider successors of v
        considerSuccessorsOfVertex(vertex);

        // If v is a root node, pop the stack and generate an SCC
        if (lowLinksOf(vertex).get().equals(indexOf(vertex).get())) {
            collectSCC(vertex);
        }
    }

    private void collectSCC(VertexClass vertex) {
        Set<VertexClass> scc = newHashSet();
        VertexClass componentVertex;
        do {
            componentVertex = stack.pop();
            scc.add(componentVertex);
        } while (componentVertex != vertex);
        stronglyConnectedComponents.add(scc);
    }

    private void considerSuccessorsOfVertex(VertexClass vertex) {
        for (VertexClass child: vertex.getChildren()) {
            if (indexOf(child).isNotDefined()) {
                // Successor w has not yet been visited; recurse on it
                strongConnect(child);
                lowLinksOf(vertex).set(Math.min(
                        lowLinksOf(vertex).get(),
                        lowLinksOf(child).get()));
            }
            else if (stack.contains(child)) {
                // Successor w is in stack S and hence in the current SCC
                lowLinksOf(vertex).set(Math.min(
                        lowLinksOf(vertex).get(),
                        indexOf(child).get()));
            }
        }
    }

    private IndexOf indexOf(VertexClass vertex) {
        return indexes.get(vertex);
    }

    private IndexOf lowLinksOf(VertexClass vertex) {
        return lowLinks.get(vertex);
    }

    private class IndexOf {
        
        private boolean defined = false;
        private Integer index = null;

        public boolean isNotDefined() {
            return !defined;
        }

        public void set(int index) {
            this.defined = true;
            this.index = index;
        }

        public Integer get() {
            return index;
        }
    }


}
