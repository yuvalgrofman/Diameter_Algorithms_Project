package graphWrappers;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import org.jgrapht.GraphType;
import org.jgrapht.graph.AbstractGraph;
import org.jgrapht.sux4j.SuccinctDirectedGraph;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

public class SuccinctDirectedGraphWrapper extends AbstractGraph<Integer, IntIntPair> {
    SuccinctDirectedGraph graph;
    Set<IntIntPair> edges = null;

    public SuccinctDirectedGraphWrapper(SuccinctDirectedGraph graph) {
        this.graph = graph;
    }

    @Override
    public Set<IntIntPair> getAllEdges(Integer sourceVertex, Integer targetVertex) {
        return graph.getAllEdges(sourceVertex, targetVertex);
    }

    @Override
    public IntIntPair getEdge(Integer sourceVertex, Integer targetVertex) {
        return graph.getEdge(sourceVertex, targetVertex);
    }

    @Override
    public Supplier<Integer> getVertexSupplier() {
        return graph.getVertexSupplier();
    }

    @Override
    public Supplier<IntIntPair> getEdgeSupplier() {
        return graph.getEdgeSupplier();
    }

    @Override
    public IntIntPair addEdge(Integer sourceVertex, Integer targetVertex) {
        IntIntPair edge = graph.addEdge(sourceVertex, targetVertex);
        if (edges != null) {
            edges.add(edge);
        }
        return edge;
    }

    @Override
    public boolean addEdge(Integer sourceVertex, Integer targetVertex, IntIntPair edge) {
        boolean bool = graph.addEdge(sourceVertex, targetVertex, edge);
        if (bool) {
            edges.add(edge);
        }
        return bool;
    }

    @Override
    public Integer addVertex() {
        return graph.addVertex();
    }

    @Override
    public boolean addVertex(Integer integer) {
        return graph.addVertex(integer);
    }

    @Override
    public boolean containsEdge(IntIntPair intIntPair) {
        return graph.containsEdge(intIntPair);
    }

    @Override
    public boolean containsVertex(Integer integer) {
        return graph.containsVertex(integer);
    }

    @Override
    public Set<IntIntPair> edgeSet() {
        if (edges == null) {
            edges = new TreeSet<>((o1, o2) -> {
                int compareFirstInt = o1.firstInt() - o2.firstInt();
                if (compareFirstInt == 0) return o1.secondInt() - o2.secondInt();
                else return compareFirstInt;
            });

            for (var v : vertexSet()) {
                edges.addAll(outgoingEdgesOf(v));
            }

//            int i = 0;
//            for (var v : vertexSet()) {
//                var edgesOfV = outgoingEdgesOf(v);
//                edges.addAll(edgesOfV);
//                var len = edgesOfV.size();
//                if (i / 1000000 != (i += len) / 1000000) System.out.println(i / 1000000);
//            }
        }
        return edges;
    }

    @Override
    public int degreeOf(Integer vertex) {
        return graph.degreeOf(vertex);
    }

    @Override
    public Set<IntIntPair> edgesOf(Integer vertex) {
        return graph.edgesOf(vertex);
    }

    @Override
    public int inDegreeOf(Integer vertex) {
        return graph.inDegreeOf(vertex);
    }

    @Override
    public Set<IntIntPair> incomingEdgesOf(Integer vertex) {
        return graph.incomingEdgesOf(vertex);
    }

    @Override
    public int outDegreeOf(Integer vertex) {
        return graph.outDegreeOf(vertex);
    }

    @Override
    public Set<IntIntPair> outgoingEdgesOf(Integer vertex) {
        return graph.outgoingEdgesOf(vertex);
    }

    @Override
    public IntIntPair removeEdge(Integer sourceVertex, Integer targetVertex) {
        IntIntPair edge = graph.removeEdge(sourceVertex, targetVertex);
        if (edge != null) edges.remove(edge);
        return edge;
    }

    @Override
    public boolean removeEdge(IntIntPair edge) {
        boolean bool = graph.removeEdge(edge);
        if (bool) edges.remove(edge);
        return bool;
    }

    @Override
    public boolean removeVertex(Integer integer) {
        return graph.removeVertex(integer);
    }

    @Override
    public Set<Integer> vertexSet() {
        return graph.vertexSet();
    }

    @Override
    public Integer getEdgeSource(IntIntPair intIntPair) {
        return graph.getEdgeSource(intIntPair);
    }

    @Override
    public Integer getEdgeTarget(IntIntPair intIntPair) {
        return graph.getEdgeTarget(intIntPair);
    }

    @Override
    public GraphType getType() {
        return graph.getType();
    }

    @Override
    public double getEdgeWeight(IntIntPair intIntPair) {
        return graph.getEdgeWeight(intIntPair);
    }

    @Override
    public void setEdgeWeight(IntIntPair intIntPair, double weight) {
        graph.setEdgeWeight(intIntPair, weight);
    }
}
