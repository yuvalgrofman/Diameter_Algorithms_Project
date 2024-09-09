package algorithmsForWebGraphs;

import algorithms.BFS;
import algorithms.Layered_BFS;
import algorithms.VertexChooser;
import it.unimi.dsi.fastutil.ints.IntIntSortedPair;
import it.unimi.dsi.webgraph.ImmutableGraph;
import org.jgrapht.Graph;
import org.jgrapht.webgraph.ImmutableDirectedGraphAdapter;
import org.jgrapht.webgraph.ImmutableUndirectedGraphAdapter;

import java.util.Set;

public class IFubForWebGraphs {
    private final ImmutableGraph g;
    private final VertexChooser chooser;
    private int numBFS;
    private int numLayeredBFS;
    private final ImmutableUndirectedGraphAdapter adapter;

    public IFubForWebGraphs(ImmutableGraph g, VertexChooser chooser) {
        this.g = g;
        this.chooser = chooser;
        adapter = new ImmutableUndirectedGraphAdapter(this.g);
    }

    public Integer run() {
        numBFS = 0;
        numLayeredBFS = 0;
        Integer u = chooser.getInitialNode(adapter);
        return run(u, 0 , 0);
    }

    public Integer run(Integer u, int l, int k) {
        int ecc_u = WebGraphBFS.calculateEccentricity(g, u);
        numBFS++;
        int i = ecc_u;
        int lb = Math.max(l, ecc_u);
        int ub = 2 * ecc_u;

        while (ub - lb > k) {
            System.out.println("ub - lb = " + ub + " - " + lb + " = " + (ub - lb));
            int bi_u = getMaxEccOfLayer(u, i);
            if (Math.max(lb, bi_u) > 2 * (i - 1)) {
                return Math.max(lb, bi_u);
            }
            lb = Math.max(lb, bi_u);
            ub = 2 * (i - 1);
            i = i - 1;
        }

        return lb;
    }

    /**
     * Let F_i be the set of vertices at distance i from u.
     * Then, get Max Ecc returns the maximum eccentricity of the vertices in F_i.
     * @param v vertex
     * @return the maximum eccentricity of the vertices in F_i
     */
    public int getMaxEccOfLayer(Integer v, int i) {
        Layered_BFS<Integer, IntIntSortedPair> bfs = new Layered_BFS<>(adapter, v, i);
        numLayeredBFS++;
        Set<Integer> layer = bfs.getLayer();

        long start, timeElapsed, millisElapsed;
        double seccondsElapsed;
        int layerSize = layer.size(), count = 1;

        int maxEcc = 0;
        for (Integer vertex : layer) {
            start = System.nanoTime();

            int ecc = WebGraphBFS.calculateEccentricity(g, vertex);
            numBFS++;

            timeElapsed = System.nanoTime() - start;
            millisElapsed = timeElapsed / 1_000_000;
            seccondsElapsed = ((double)millisElapsed) / 1_000;
            System.out.println("finished " + count++ + " / " + layerSize + " from current layer in " + seccondsElapsed + " seconds");


            if (ecc > maxEcc) {
                maxEcc = ecc;
            }
        }

        return maxEcc;
    }

    public int getNumBFS() {
        return numBFS;
    }

    public int getNumLayeredBFS() {
        return numLayeredBFS;
    }
}
