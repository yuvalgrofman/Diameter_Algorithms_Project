package algorithmsForWebGraphs;

import algorithms.NeighborsBFS;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.NodeIterator;
import it.unimi.dsi.webgraph.Transform;
import org.jgrapht.webgraph.ImmutableDirectedGraphAdapter;

import java.util.*;

public class RoddityVirginiaForWebGraph {

    private final int s;
    private int numBFS = 0;
    private final ImmutableGraph g;

    public RoddityVirginiaForWebGraph(ImmutableGraph g, int s) {
        this.g = g;
        this.s = s;
    }



    public Integer run() {
        numBFS = 0;
        ImmutableGraph reverse = Transform.transpose(g);
        int maxEcc = 0;

        // Compute Hitting Set of size n/s * log(n)
//        Set<Integer> hittingSet = new HashSet<>();
        //int[] hittingSet = new int[g.numNodes()];
        LinkedList<Integer> hittingSet = new LinkedList<>();

    
        int bound = (int) (Integer.MAX_VALUE * (Math.log(g.numNodes()) / s));

        for (NodeIterator it = g.nodeIterator(); it.hasNext(); ) {
            int v = it.nextInt();
            Random rand = new Random();
            if (rand.nextInt(Integer.MAX_VALUE) < bound) {
                hittingSet.add(v);
            }
        }

        int hittingSetSize = hittingSet.size();
        System.out.println("hitting set of size: " + hittingSet.size());

        // Compute BFS from hitting set
        int i = 1;
        for (Integer v : hittingSet) {
            long start = System.nanoTime();

            maxEcc = Math.max(maxEcc, WebGraphBFS.calculateEccentricity(g, v));
            numBFS++;

            long timeElapsed = System.nanoTime() - start;
            long millisElapsed = timeElapsed / 1_000_000;
            double seccondsElapsed = ((double)millisElapsed) / 1_000;
            System.out.println("finished " + i++ + " / " + hittingSetSize + " from the hitting set after " + seccondsElapsed + " secconds");
        }

        //Find vertex furthest from hitting set
        long start = System.nanoTime();

        Integer w = WebGraphBFS.bfsFromQueue(reverse, hittingSet).getFurthestVertex();

        numBFS++;

        long timeElapsed = System.nanoTime() - start;
        long millisElapsed = timeElapsed / 1_000_000;
        double seccondsElapsed = ((double)millisElapsed) / 1_000;
        System.out.println("finished finding w using a bfs in " + seccondsElapsed + " secconds");



        // Compute BFS_in from every vertex in N_s(w)
        ImmutableDirectedGraphAdapter adapter = new ImmutableDirectedGraphAdapter(g, reverse);
        NeighborsBFS<Integer, IntIntPair> neighborsBFS = new NeighborsBFS<>(adapter, w, s);
        Set<Integer> neighbours = neighborsBFS.getNeighbors();
        adapter = null;
        neighborsBFS = null;

        final int numNeighbours = neighbours.size();
        i = 1;
        for (Integer v : neighbours) {
            start = System.nanoTime();

            maxEcc = Math.max(maxEcc, WebGraphBFS.calculateEccentricity(reverse, v));
            numBFS++;

            timeElapsed = System.nanoTime() - start;
            millisElapsed = timeElapsed / 1_000_000;
            seccondsElapsed = ((double)millisElapsed) / 1_000;
            System.out.println("finished " + i++ + " / " + numNeighbours + " from the neighbours of w in " + seccondsElapsed + " secconds");
        }

        return maxEcc;
    }

    public int getNumBFS() {
        return numBFS;
    }
}
