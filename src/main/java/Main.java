import it.unimi.dsi.webgraph.ImmutableGraph;
import jdk.dynalink.linker.support.SimpleLinkRequest;
import org.apache.commons.math3.geometry.spherical.twod.Vertex;
import org.jgrapht.Graph;
import org.jgrapht.graph.*;
import org.jgrapht.webgraph.ImmutableDirectedGraphAdapter;
import graphParsers.GraphFromTextParser;
import java.io.IOException;
import java.net.*;
import java.sql.SQLOutput;

import specialScanner.SpecializedIntegerScanner;

class Main {
    private static final int NUM_TESTS = 10;
    public static void main(String[] args) throws IOException {
//        ImmutableGraph graph = ImmutableGraph.loadMapped("/Users/yuvalgrofman/Documents/barilan/3/DA/development/db/eu-2005/eu-2005");
//        ImmutableDirectedGraphAdapter adapter = new ImmutableDirectedGraphAdapter(graph);

//        GraphFromTextParser<Integer, DefaultEdge, AbstractBaseGraph<Integer, DefaultEdge>> parser = new GraphFromTextParser();
//        AbstractBaseGraph<Integer, DefaultEdge> g = parser.parse("/Users/yuvalgrofman/Documents/barilan/3/DA/development/db/snap/deezer_clean_data/HR_edges.csv", new DefaultUndirectedGraph<Integer, DefaultEdge>(DefaultEdge.class), new SpecializedIntegerScanner());

        GraphFromTextParser<Integer, DefaultEdge, AbstractBaseGraph<Integer, DefaultEdge>> parser = new GraphFromTextParser();
        AbstractBaseGraph<Integer, DefaultEdge> g = parser.parse("/Users/yuvalgrofman/Documents/barilan/3/DA/development/db/snap/facebook_combined.txt", new DefaultUndirectedGraph<Integer, DefaultEdge>(DefaultEdge.class), new SpecializedIntegerScanner());

//        VertexChooser vc = new Random_Vertex();
//        IFub<Integer, DefaultEdge> ifub = new IFub<>(g, vc);
//
//        for (int i = 0; i < NUM_TESTS; i++) {
//            // Check algorithm runtime
//            long startTime = System.currentTimeMillis();
//            System.out.println("Diameter: " + ifub.run());
//            long endTime = System.currentTimeMillis();
//            System.out.println("Random " + (i + 1) + " took " + (endTime - startTime) + "ms");
//            System.out.println("Number of BFS: " + ifub.getNumBFS());
//            System.out.println("Number of Layered BFS: " + ifub.getNumLayeredBFS());
//        }
//
//        vc = new Highest_Degree();
//
//        long startTime = System.currentTimeMillis();
//        System.out.println("Diameter: " + ifub.run());
//        long endTime = System.currentTimeMillis();
//        System.out.println("Highest Degree took " + (endTime - startTime));
//        System.out.println("Number of BFS: " + ifub.getNumBFS());
//        System.out.println("Number of Layered BFS: " + ifub.getNumLayeredBFS());
//
//        vc = new Arbitrary_Vertex();
//        startTime = System.currentTimeMillis();
//        System.out.println("Diameter: " + ifub.run());
//        endTime = System.currentTimeMillis();
//        System.out.println("Arbitrary Vertex took " + (endTime - startTime));
//        System.out.println("Number of BFS: " + ifub.getNumBFS());
//        System.out.println("Number of Layered BFS: " + ifub.getNumLayeredBFS());

//        RodittyVirginia<Integer,DefaultEdge> rv = new RodittyVirginia<Integer, DefaultEdge>(g, (int) Math.sqrt(g.vertexSet().size()));
//
//        for (int i = 0; i < NUM_TESTS; i++) {
//            // Check algorithm runtime
//            long startTime = System.currentTimeMillis();
//            System.out.println("Diameter: " + rv.run());
//            long endTime = System.currentTimeMillis();
//            System.out.println("Random " + (i + 1) + " took " + (endTime - startTime));
//            System.out.println("Number of BFS: " + rv.getNumBFS());
//        }


//        ApproxDiamSparse<Integer, DefaultEdge> ads = new ApproxDiamSparse<Integer, DefaultEdge>(g, -1);
//        long startTime = System.currentTimeMillis();
//        System.out.println("Diameter: " + ads.run());
//        long endTime = System.currentTimeMillis();
//        System.out.println("ApproxDiamSparse took " + (endTime - startTime) + "ms");
//        System.out.println("Number of BFS: " + ads.getNumBFS());

        for (int i = 0; i < NUM_TESTS; i++) {
            // Check algorithm runtime
            long startTime = System.currentTimeMillis();
            ApproxDiamSparse<Integer, DefaultEdge> ads = new ApproxDiamSparse<Integer, DefaultEdge>(g, -1);
            System.out.println("Diameter: " + ads.run());
            long endTime = System.currentTimeMillis();
            System.out.println("Run " + (i + 1) + " took " + (endTime - startTime) + "ms");
            System.out.println("Number of BFS: " + ads.getNumBFS());
        }

//        int maxEcc = 0;
//        int i = 0;
//        for (Integer vertex : g.vertexSet()) {
//            BFS<Integer, DefaultEdge> bfs = new BFS<>(g, vertex);
//            maxEcc = Math.max(maxEcc, bfs.getEcc());
//            if (++i % 1000 == 0) {
//                System.out.println("Processed " + i + " vertices");
//                System.out.println("Max Ecc: " + maxEcc);
//            }
//        }
    }
}