import com.martiansoftware.jsap.JSAPException;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.ImmutableSubgraph;
import it.unimi.dsi.webgraph.Transform;
import it.unimi.dsi.webgraph.algo.StronglyConnectedComponents;
import it.unimi.dsi.webgraph.algo.SumSweepDirectedDiameterRadius;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.sux4j.SuccinctDirectedGraph;
import org.jgrapht.webgraph.ImmutableDirectedGraphAdapter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException, JSAPException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
//        BVGraph.main(new String[]{"-o", "-O", "-L", "/home/jonathan/Downloads/indochina-2004"});
//        BVGraph.main(new String[]{"-o", "-O", "-L", "/home/jonathan/Downloads/indochina-2004-t"});

//        ImmutableGraph webGraph = ImmutableGraph.load("/home/jonathan/Downloads/indochina-2004");
//        ImmutableGraph webGraphRev = ImmutableGraph.load("/home/jonathan/Downloads/indochina-2004-t");
//        ImmutableDirectedGraphAdapter graphAdapter = new ImmutableDirectedGraphAdapter(webGraph, webGraphRev);
//
//
//        ConnectivityInspector<Integer, IntIntPair> inspector = new ConnectivityInspector<>(graphAdapter);
//        var largestWcc = Collections.max(inspector.connectedSets(), Comparator.comparingInt(Set::size));
//        int[] arr = new int[largestWcc.size()];
//        int i = 0;
//        for (int v : largestWcc) {
//            arr[v] = i;
//            i++;
//        }
//        System.out.println(graphAdapter.vertexSet().size());
//        Arrays.sort(arr);
//        ImmutableSubgraph subgraph = new ImmutableSubgraph(webGraph, arr);
//        ImmutableSubgraph subgraphRev = new ImmutableSubgraph(webGraphRev, arr);
//        arr = null;
//
//        subgraph.save("/home/jonathan/Downloads/indochina-2004-largewcc");
//        subgraphRev.save("/home/jonathan/Downloads/indochina-2004-largewcc-rev");
//        System.out.println("sasas");
//        graphAdapter = new ImmutableDirectedGraphAdapter(subgraph, subgraphRev);
//
//
//        System.out.println("finished building graph");
//        System.out.println(graphAdapter.vertexSet().size());

        ImmutableGraph webGraph = ImmutableGraph.load("/home/jonathan/Downloads/indochina-2004-largewcc");
        ImmutableGraph webGraphRev = ImmutableGraph.load("/home/jonathan/Downloads/indochina-2004-largewcc-rev");
        ImmutableDirectedGraphAdapter graphAdapter = new ImmutableDirectedGraphAdapter(webGraph, webGraphRev);
        IntIntPair pair = graphAdapter.iterables().edges().iterator().next();
        System.out.println(pair);
        System.out.println(graphAdapter.getEdgeTarget(pair));

        ExactSumSweepForIntegerGraphs exactSumSweep = new ExactSumSweepForIntegerGraphs(graphAdapter, new ProgressLogger(), ExactSumSweepForIntegerGraphs.OutputLevel.RADIUS_DIAMETER, null);
        exactSumSweep.compute();

//        SumSweepDirectedDiameterRadius s = new SumSweepDirectedDiameterRadius(immutableGraph, SumSweepDirectedDiameterRadius.OutputLevel.RADIUS_DIAMETER, null, new ProgressLogger());
//        s.compute();
    }
}