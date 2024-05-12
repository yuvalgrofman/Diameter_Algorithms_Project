import it.unimi.dsi.webgraph.ImmutableGraph;
import org.apache.commons.math3.geometry.spherical.twod.Vertex;
import org.jgrapht.webgraph.ImmutableDirectedGraphAdapter;

import java.io.IOException;
import java.net.*;

class Main {
    private static final int NUM_TESTS = 10;
    public static void main(String[] args) throws IOException {
        ImmutableGraph graph = ImmutableGraph.loadMapped("/Users/yuvalgrofman/Documents/barilan/3/DA/development/db/cnr-2000/cnr-2000");
        ImmutableDirectedGraphAdapter adapter = new ImmutableDirectedGraphAdapter(graph);

        System.out.println("Number of vertices: " + adapter.vertexSet().size());
        System.out.println("Number of edges: " + adapter.edgeSet().size());

        int maxDegree = 0;
        for (int i = 0; i < adapter.vertexSet().size(); i++) {
            maxDegree = Math.max(maxDegree, adapter.outDegreeOf(i));
        }
//        Integer maxDegreeWithHD = new Highest_Degree().getInitialNode(adapter);

        System.out.println("Max degree Naive: " + maxDegree);
//        System.out.println("Max degree with Highest Degree: " + maxDegreeWithHD);

        // Checking Vertex Choosers
//        VertexChooser hd = new Highest_Degree();
        VertexChooser rv = new Random_Vertex();
        VertexChooser av = new Arbitrary_Vertex();

        for (int i = 0; i < NUM_TESTS; i++) {
            System.out.println("Random Vertex: " + rv.getInitialNode(adapter));
            System.out.println("Arbitrary Vertex: " + av.getInitialNode(adapter));
        }
        int diameter = 0;

        // Random BFS to get diameter
        for (int i = 0; i < NUM_TESTS; i++) {
            BFS bfs = new BFS(adapter, rv.getInitialNode(adapter));
            int ecc = bfs.getEcc();
            Object furthest = bfs.getFurthest();
            int depth = bfs.getDepth(furthest);
            assert(ecc == depth);
            diameter = Math.max(diameter, ecc);
        }

        System.out.println("Diameter: " + diameter);
    }
}