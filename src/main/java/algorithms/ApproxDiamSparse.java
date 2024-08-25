package algorithms;

import org.jgrapht.Graph;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.*;

public class ApproxDiamSparse<V,E> extends Diameter_Algorithm<V, E> {
    private int h;
    private int numBFS = 0;

    public ApproxDiamSparse(AbstractBaseGraph<V, E> g, int h) {
        super(g);
        this.h = h;
    }

    public Integer run(int minDegree) {
        Graph<V,E> reverse = new EdgeReversedGraph<>(super.getGraph());
        Set<V> H = new HashSet<>();
        int maxEcc = 0;

        // Compute H
        for (V v : super.getGraph().vertexSet()) {
            if (super.getGraph().degreeOf(v) >= minDegree) {
                H.add(v);
            }
        }

        // Compute BFS from every vertex in H
        for (V v : H) {
            BFS<V,E> bfs = new BFS<>(super.getGraph(), v);
            maxEcc = Math.max(maxEcc, bfs.getEcc());
            numBFS++;
        }

        // Create a new graph to find vertex furthest from minimum degree set
        Graph <V,E> extendedGraph = new EdgeReversedGraph<V,E>((AbstractBaseGraph<V,E>) super.getGraph().clone());
        V r = (V) new Integer(121891731);
        extendedGraph.addVertex(r);
        for (V u : H) {
            extendedGraph.addEdge(r ,u);
        }

        // Find vertex furthest from hitting set
        BFS<V,E> bfs = new BFS<>(extendedGraph, r);
        numBFS++;
        V w = bfs.getFurthest();
        int dist = bfs.getDepth(w) - 1;

        // Compute BFS_out from w
        BFS<V,E> bfsOut = new BFS<>(super.getGraph(), w);
        numBFS++;
        maxEcc = Math.max(maxEcc, bfsOut.getEcc());

        // Calculate bfsRadius and perform BFS from every vertex with distance < radius from w
        int bfsRadius = Math.min(h + 1, dist);
        BreadthFirstIterator<V,E> bfsIterator = new BreadthFirstIterator<>(super.getGraph(), w);
        V currentVertex = bfsIterator.next();
        while (bfsIterator.hasNext() && bfsIterator.getDepth(currentVertex) <= bfsRadius) {
            BFS<V,E> bfsIn = new BFS<>(reverse, currentVertex);
            maxEcc = Math.max(maxEcc, bfsIn.getEcc());
            numBFS++;
            currentVertex = bfsIterator.next();
        }

        return maxEcc;
    }

    @Override
    public Integer run() {
        VertexChooser chooser = new Arbitrary_Vertex();
        BFS<V,E> bfs = new BFS<>(super.getGraph(), chooser.getInitialNode(super.getGraph()));
        if (h < 0) {
            h = 2 * bfs.getEcc() / 3;
        }
        int diameter = run((int) Math.round(Math.pow(super.getGraph().edgeSet().size(), 1 / (2 * h + 3))) + 1);
        numBFS++;
        return diameter;
    }

    public int getNumBFS() {
        return numBFS;
    }
}