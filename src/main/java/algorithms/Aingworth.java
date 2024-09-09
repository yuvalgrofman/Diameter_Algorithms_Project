//import org.jgrapht.Graph;
//import org.jgrapht.graph.EdgeReversedGraph;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
//public class Aingworth<V,E> extends Diameter_Algorithm<V, E> {
//    private final int s;
//
//    public Aingworth(Graph<V, E> g, int s) {
//        super(g);
//        this.s = s;
//    }
//
//    @Override
//    public Integer run() {
//        Graph<V,E> reverse = new EdgeReversedGraph<>(super.getGraph());
//
//        algorithms.NeighborsBFS<V,E>[] arr = new algorithms.NeighborsBFS[super.getGraph().vertexSet().size()];
//        Map<V, algorithms.NeighborsBFS<V,E>> map = new HashMap<>();
//        V w = null;
//        int depth = 0;
//        int maxEcc = 0;
//        for (V v : super.getGraph().vertexSet()) {
//            map.put(v, new algorithms.NeighborsBFS<>(super.getGraph(), v, s));
//            if (map.get(v).getDepth() > depth) {
//                depth = map.get(v).getDepth();
//                w = v;
//            }
//        }
//
//        BFS<V,E> bfs = new BFS<>(super.getGraph(), w);
//        maxEcc = bfs.getEcc();
//
//        for (V v : map.get(w).getNeighbors()) {
//            BFS<V,E> reverseBFS = new BFS<V,E>(reverse, v);
//            maxEcc = Math.max(maxEcc, reverseBFS.getEcc());
//        }
//
//        // Compute Hitting Set of size s^-1 nlogn
//
//        Set<V> hittingSet = new HashSet<>();
//
//        for (V v : map.get(w).getNeighbors()) {
//            hittingSet.add(v);
//        }
//
//        return 0;
//    }
//}
