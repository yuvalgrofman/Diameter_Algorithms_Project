import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.Transform;
import it.unimi.dsi.webgraph.algo.SumSweepDirectedDiameterRadius;
import org.jgrapht.webgraph.ImmutableDirectedGraphAdapter;
import java.io.IOException;

class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ImmutableGraph immutableGraph = ImmutableGraph.load("/home/jonathan/Downloads/inputWebgraph/Directed/amazon0505");
        ImmutableGraph immutableGraphRev = Transform.transpose(immutableGraph);
        ImmutableDirectedGraphAdapter graphAdapter = new ImmutableDirectedGraphAdapter(immutableGraph, immutableGraphRev);
//        SuccinctDirectedGraph graph = new SuccinctDirectedGraph(graphAdapter);
        System.out.println("finished building graph");

        ExactSumSweepForIntegerGraphs exactSumSweep = new ExactSumSweepForIntegerGraphs(graphAdapter, new ProgressLogger(), ExactSumSweepForIntegerGraphs.OutputLevel.RADIUS_DIAMETER, null);
        exactSumSweep.compute();

        SumSweepDirectedDiameterRadius s = new SumSweepDirectedDiameterRadius(immutableGraph, SumSweepDirectedDiameterRadius.OutputLevel.RADIUS_DIAMETER, null, new ProgressLogger());
//        s.compute();

    }
}