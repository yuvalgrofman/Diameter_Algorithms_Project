package algorithms;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.EdgeReversedGraph;

import java.util.*;

public class TwoApproxEcc<V,E> extends Diameter_Algorithm<V,E> {
    private int numBFS;
    private HashMap<V, Integer> approxEcc;

    public TwoApproxEcc(AbstractBaseGraph<V,E> graph) {
        super(graph);
    }

    @Override
    public Integer run() {
        numBFS = 0;
        Graph<V,E> reverse = new EdgeReversedGraph<>(super.getGraph());
        int maxEcc = 0;

        // Compute Hitting Set of size sqrt(n) * log(n)
        Set<V> hittingSet = new HashSet<>();

        int bound = (int) (Integer.MAX_VALUE * (Math.log(super.getGraph().vertexSet().size()) / Math.sqrt(super.getGraph().vertexSet().size())));

        // Thus, the range of acceptance [0, MAX * (log(n) / sqrt(n))]
        // And so, the probability of acceptance is log(n) / sqrt(n)
        // So the expected size of the hitting set is sqrt(n) * log(n)

        for (V v : super.getGraph().vertexSet()) {
            Random rand = new Random();
            if (rand.nextInt(Integer.MAX_VALUE) <  bound) {
                hittingSet.add(v);
            }
        }

        // Find the vertex furthest from the hitting set using dijkstra's algorithm
        Graph<V,E> extendedGraph = (Graph<V, E>) super.getGraph().clone();
        V r = (V) new Integer(121891731);
        extendedGraph.addVertex(r);
        for (V u : hittingSet) {
            extendedGraph.addEdge(r ,u);
        }

        DijkstraShortestPath<V,E> dijkstra = new DijkstraShortestPath<>(extendedGraph);
        ShortestPathAlgorithm.SingleSourcePaths<V,E> paths = dijkstra.getPaths(r);

        V furthest = null;
        int weight = -1;
        for (V v : super.getGraph().vertexSet()) {
            if (paths.getWeight(v) > weight) {
                weight = (int) paths.getWeight(v);
                furthest = v;
            }
        }

        // Calculate sqrt(n) closest neighbors of the furthest vertex
        NeighborsBFS<V,E> neighborsBFS = new NeighborsBFS<>(reverse, furthest, (int) Math.sqrt(super.getGraph().vertexSet().size()));
        Set<V> neighbors = neighborsBFS.getNeighbors();

        // Run dijkstra from every vertex in the set of neighbors
        for (V v : neighbors) {
            ShortestPathAlgorithm.SingleSourcePaths<V,E> neighborPaths = dijkstra.getPaths(v);

            int ecc = 0;
            for (V u : super.getGraph().vertexSet()) {
                ecc = Math.max(ecc, (int) neighborPaths.getWeight(u));
            }
            approxEcc.put(v, ecc);
            maxEcc = Math.max(maxEcc, ecc);
        }

        // Run incoming dijkstra from every vertex in hitting set and from furthest
        DijkstraShortestPath<V,E> incomingDijkstra = new DijkstraShortestPath<>(reverse);
        Map<V, ShortestPathAlgorithm.SingleSourcePaths<V,E>> incomingPaths = new HashMap<>();
        for (V v : hittingSet) {
            incomingPaths.put(v, incomingDijkstra.getPaths(v));
        }

        incomingPaths.put(furthest, incomingDijkstra.getPaths(furthest));

        int ecc = 0;
        // Approximate eccentricity of every vertex which is not in the neighbors set
        for (V v : super.getGraph().vertexSet()) {
            if (!neighbors.contains(v)) {
                ecc = 0;
                for (V s : hittingSet) {
                    ecc = Math.max(ecc, (int) incomingPaths.get(s).getWeight(v));
                }
                ecc = Math.max(ecc, (int) incomingPaths.get(furthest).getWeight(v));
                approxEcc.put(v, ecc);
                maxEcc = Math.max(maxEcc, ecc);
            }
        }

        return maxEcc;
    }
}
