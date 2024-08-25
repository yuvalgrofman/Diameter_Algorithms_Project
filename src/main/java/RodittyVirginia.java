import org.jgrapht.Graph;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.EdgeReversedGraph;

import java.util.*;

public class RodittyVirginia<V,E> extends Diameter_Algorithm<V, E> {
    private final int s;
    private int numBFS = 0;

    public RodittyVirginia(AbstractBaseGraph<V, E> g, int s) {
        super(g);
        this.s = s;
    }

    @Override
    public Integer run() {
        numBFS = 0;
        Graph<V,E> reverse = new EdgeReversedGraph<>(super.getGraph());
        int maxEcc = 0;

        // Compute Hitting Set of size n/s * log(n)
        Set<V> hittingSet = new HashSet<>();


        int bound = (int) (Integer.MAX_VALUE * (Math.log(super.getGraph().vertexSet().size()) / s));

        for (V v : super.getGraph().vertexSet()) {
            Random rand = new Random();
            if (rand.nextInt(Integer.MAX_VALUE) <  bound) {
                hittingSet.add(v);
            }
        }

        // Compute BFS from hitting set
        for (V v : hittingSet) {
            BFS<V,E> bfs = new BFS<>(super.getGraph(), v);
            maxEcc = Math.max(maxEcc, bfs.getEcc());
            numBFS++;
        }

        // Create a new graph to find vertex furthest from hitting set
        Graph <V,E> extendedGraph = new EdgeReversedGraph<>(super.getGraph());
        V r = (V) new Integer(121891731);
        extendedGraph.addVertex(r);
        for (V u : hittingSet) {
            extendedGraph.addEdge(r ,u);
        }

        // Find vertex furthest from hitting set
        BFS<V,E> bfs = new BFS<>(extendedGraph, r);
        numBFS++;
        V w = bfs.getFurthest();

        // Compute BFS_in from every vertex in N_s(w)
        NeighborsBFS<V,E> neighborsBFS = new NeighborsBFS<>(super.getGraph(), w, s);

        for (V v : neighborsBFS.getNeighbors()) {
            BFS<V,E> reverseBFS = new BFS<V,E>(reverse, v);
            maxEcc = Math.max(maxEcc, reverseBFS.getEcc());
            numBFS++;
        }

        return maxEcc;
    }

    public int getNumBFS() {
        return numBFS;
    }
}