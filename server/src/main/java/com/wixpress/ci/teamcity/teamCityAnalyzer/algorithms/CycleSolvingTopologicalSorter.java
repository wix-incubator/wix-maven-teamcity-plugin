package com.wixpress.ci.teamcity.teamCityAnalyzer.algorithms;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * topological sort algorithm who handles cycles by removing edges from the graph until it has no more cycles.
 * The removed edges are marked on the vertices as removed.
 * @author yoav
 * @since 4/2/12
 */
public class CycleSolvingTopologicalSorter<VertexClass extends Vertex<VertexClass> & IgnoreEdgesCapable<VertexClass>> {

    private Collection<VertexClass> vertices;

    public CycleSolvingTopologicalSorter(Collection<VertexClass> vertices) {
        this.vertices = vertices;
    }
    
    public List<VertexClass> sort() {
        Set<Set<VertexClass>> sccs = targanStronglyConnectedComponents();
        int cyclesLimit = 100;
        while (sccs.size() < vertices.size()) {
            breakCycles(sccs);
            sccs = targanStronglyConnectedComponents();
            if (cyclesLimit-- < 0)
                throw new CycleSolvingTopologicalSorterException("failed topological sorting - reached limit of 100 tries to remove edges");
        }
        return topologicalSort();
    }

    private List<VertexClass> topologicalSort() {
        return new TopologicalSorter<VertexClass>(vertices).sort();
    }

    private void breakCycles(Set<Set<VertexClass>> sccs) {
        for (Set<VertexClass> scc: sccs) {
            if (scc.size() > 1) {
                Iterator<VertexClass> iterator = scc.iterator();
                VertexClass first = iterator.next();
                VertexClass second = iterator.next();
                second.ignoreEdgeTo(first);
                first.ignoreEdgeTo(second);
            }
        }
    }

    private Set<Set<VertexClass>> targanStronglyConnectedComponents() {
        TarganSCCsAlgorithm<VertexClass> targan = new TarganSCCsAlgorithm<VertexClass>(vertices);
        return targan.sccs();
    }
}
