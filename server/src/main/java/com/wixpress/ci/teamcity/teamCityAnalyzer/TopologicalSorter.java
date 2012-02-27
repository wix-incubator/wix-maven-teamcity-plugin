package com.wixpress.ci.teamcity.teamCityAnalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

/**
 * @author yoav
 * @since 2/26/12
 */
public class TopologicalSorter<NodeClass extends TopologicalSorter.Node<NodeClass>> {
    private Node vertices[]; // list of vertices

    private int edges[][]; // adjacency edges

    private int numVerts; // current number of vertices

    private Node[] sortedArray;

    public TopologicalSorter(int size) {
        vertices = new Node[size];
        edges = new int[size][size];
        sortedArray = new Node[size]; // sorted vert labels
    }

    public TopologicalSorter(Collection<NodeClass> nodes) {
        this(nodes.size());

        Map<NodeClass, Integer> indexes = newHashMap();

        // add vertices
        for (NodeClass node : nodes) {
            indexes.put(node, addVertex(node));
        }

        // add edges
        for (NodeClass node: nodes) {
            for (NodeClass nodeOutgoingEdges: node.getChildren()) {
                if (indexes.containsKey(nodeOutgoingEdges))
                    addEdge(indexes.get(node), indexes.get(nodeOutgoingEdges));
            }
        }
    }

    public int addVertex(NodeClass buildTypeId) {
        vertices[numVerts] = buildTypeId;
        return numVerts++;
    }

    public void addEdge(int start, int end) {
        edges[start][end] = 1;
    }

    public List<NodeClass> sort() // toplogical sort
    {
        while (numVerts > 0) // while vertices remain,
        {
            // get a vertex with no successors, or -1
            int currentVertex = noSuccessors();
            if (currentVertex == -1) // must be a cycle
            {
                throw new IllegalStateException("BuildTypes have a cyclical dependencies");
            }
            // insert vertex label in sorted array (start at end)
            sortedArray[numVerts - 1] = vertices[currentVertex];

            deleteVertex(currentVertex); // delete vertex
        }

        ArrayList<NodeClass> result = newArrayList();
        for (Node node: sortedArray)
            //noinspection unchecked
            result.add((NodeClass) node);
        return result;
    }

    public int noSuccessors() // returns vert with no successors (or -1 if no such verts)
    {
        boolean isEdge; // edge from row to column in adjMat

        for (int row = 0; row < numVerts; row++) {
            isEdge = false; // check edges
            for (int col = 0; col < numVerts; col++) {
                if (edges[row][col] > 0) // if edge to another,
                {
                    isEdge = true;
                    break; // this vertex has a successor try another
                }
            }
            if (!isEdge) // if no edges, has no successors
                return row;
        }
        return -1; // no
    }

    private void deleteVertex(int delVert) {
        if (delVert != numVerts - 1) // if not last vertex, delete from vertices
        {
            System.arraycopy(vertices, delVert + 1, vertices, delVert, numVerts - 1 - delVert);

            for (int row = delVert; row < numVerts - 1; row++)
                moveRowUp(row, numVerts);

            for (int col = delVert; col < numVerts - 1; col++)
                moveColLeft(col, numVerts - 1);
        }
        numVerts--; // one less vertex
    }

    private void moveRowUp(int row, int length) {
        System.arraycopy(edges[row + 1], 0, edges[row], 0, length);
    }

    private void moveColLeft(int col, int length) {
        for (int row = 0; row < length; row++)
            edges[row][col] = edges[row][col + 1];
    }

    public interface Node<NodeClass extends Node> {
        public int hashCode();
        public boolean equals(Object other);

        Iterable<NodeClass> getChildren();
    }

}
