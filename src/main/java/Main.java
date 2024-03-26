import com.martiansoftware.jsap.JSAPException;
import edu.uci.ics.jung.graph.DirectedGraph;
import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import it.unimi.dsi.fastutil.longs.LongLongSortedPair;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.webgraph.ImmutableDirectedBigGraphAdapter;
import org.jgrapht.webgraph.ImmutableUndirectedBigGraphAdapter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

class Main {
    public static void main(String[] args) throws JSAPException, IOException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
//        try {
//            BVGraph.main(new String[]{"-o", "-O", "-L", "cnr-2000"});
//        } catch (Exception exception) {
//            System.err.println("BVGraph.main exception");
//        }

        ImmutableGraph g = ImmutableGraph.load("cnr-2000");

        var g2 = new ImmutableDirectedBigGraphAdapter(g);
        System.out.println(g2.edgeSet().size());
        System.out.println(g2.vertexSet().size());

//        DirectedGraph<Long, LongLongPair> g = new SimpleDirectedGraph<Long,LongLongPair>();


        AsUndirectedGraph<Long, LongLongPair> g3 = new AsUndirectedGraph<>(g2);
        System.out.println(g3.vertexSet().size());
        System.out.println(g3.edgeSet().size());


        System.out.println(new BFS<>(g3, new Random_Vertex().getInitialNode(g3)).getEcc());
//        System.out.println(new BFS<>(g3, new Random_Vertex().getInitialNode(g3)).getEcc());
//        System.out.println(new BFS<>(g3, new Random_Vertex().getInitialNode(g3)).getEcc());

        System.out.println(g2.getType());

        System.out.println("done");
//        System.out.println(new BFS<>(g2, ).getEcc());


//        ConnectivityInspector<Long, LongLongPair> c = new ConnectivityInspector<>(g3);

//        Long v = (long)-1;
//        for (int i = 0; i < 100; i++) {
//            v = new Random_Vertex().getInitialNode(g2);
//            System.out.println(c.connectedSetOf(v).size());
//        }
    }
}