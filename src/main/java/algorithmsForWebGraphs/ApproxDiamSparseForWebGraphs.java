package algorithmsForWebGraphs;

import algorithms.Arbitrary_Vertex;
import algorithms.BFS;
import algorithms.VertexChooser;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.NodeIterator;
import it.unimi.dsi.webgraph.Transform;
import org.jgrapht.Graph;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.webgraph.ImmutableDirectedGraphAdapter;

import java.util.*;

public class ApproxDiamSparseForWebGraphs {
    private int h;
    private int numBFS = 0;
    private final ImmutableGraph g;
    private final ImmutableGraph reverse;

    public ApproxDiamSparseForWebGraphs(ImmutableGraph g, int h) {
        this.g = g;
        this.h = h;
        this.reverse = Transform.transpose(g);
    }

    public ApproxDiamSparseForWebGraphs(ImmutableGraph g) {
        this(g, -1);
    }

    public Integer run(int minDegree) {
        LinkedList<Integer> H = new LinkedList<>();
        int maxEcc = 0;

        // Compute H
        for (NodeIterator it = g.nodeIterator(); it.hasNext(); ) {
            int v = it.nextInt();
//            if (g.outdegree(v) + reverse.outdegree(v) >= minDegree) { //TODO: check if this was the thing we should have done

            if (g.outdegree(v) >= minDegree) { //TODO: check if this was the thing we should have done
                H.add(v);
            }
        }

        // Compute BFS from every vertex in H
        int i = 1;
        int hSize = H.size();
        for (Integer v : H) {
            long start = System.nanoTime();

            maxEcc = Math.max(maxEcc, WebGraphBFS.calculateEccentricity(g, v));
            numBFS++;

            long timeElapsed = System.nanoTime() - start;
            long millisElapsed = timeElapsed / 1_000_000;
            double seccondsElapsed = ((double)millisElapsed) / 1_000;
            System.out.println("finished " + i++ + " / " + hSize + " from H after " + seccondsElapsed + " secconds");

        }

        // Find vertex furthest from hitting set
        long start = System.nanoTime();

        WebGraphBFS.BFSResult bfsResult = WebGraphBFS.bfsFromQueue(reverse, H);
        numBFS++;
        Integer w = bfsResult.getFurthestVertex();
        int dist = bfsResult.getMaxDepth();

        long timeElapsed = System.nanoTime() - start;
        long millisElapsed = timeElapsed / 1_000_000;
        double seccondsElapsed = ((double)millisElapsed) / 1_000;
        System.out.println("finished finding the furthest from the hitting set (w) after " + seccondsElapsed + " secconds");



        // Compute BFS_out from w
        start = System.nanoTime();

        int bfsOutEcc = WebGraphBFS.calculateEccentricity(g, w);
        numBFS++;
        maxEcc = Math.max(maxEcc, bfsOutEcc);

        timeElapsed = System.nanoTime() - start;
        millisElapsed = timeElapsed / 1_000_000;
        seccondsElapsed = ((double)millisElapsed) / 1_000;
        System.out.println("finished bfsOUT from w after " + seccondsElapsed + " secconds");

        // Calculate bfsRadius and perform BFS from every vertex with distance < radius from w
        int bfsRadius = Math.min(h + 1, dist);


        Queue<Integer> queue1 = new LinkedList<>();
        Queue<Integer> queue2 = new LinkedList<>();

        Set<Integer> visited = new HashSet<>();
        Map<Integer, Integer> distances = new HashMap<>();

        queue1.add(w);
        queue2.add(w);
        visited.add(w);
        distances.put(w, 0);

        int current, currentDistance, queueSize = 1;
        while (!queue2.isEmpty()) {
            current = queue2.poll();
            currentDistance = distances.get(current);

            if (currentDistance > bfsRadius) {
                break;
            }

            if (currentDistance + 1 <= bfsRadius) {
                int[] neighbors = g.successorArray(current);
                for (int neighbor : neighbors) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue2.add(neighbor);
                        queue1.add(neighbor);
                        distances.put(neighbor, currentDistance + 1);
                        queueSize++;
                    }
                }
            }
        }
        visited = null;
        queue2 = null;

        i = 1;
        int bfsInEcc;
        while (!queue1.isEmpty()) {
            current = queue1.poll();
            currentDistance = distances.get(current);

            if (currentDistance > bfsRadius) { //shouldn't happen
                break;
            }


            start = System.nanoTime();

            bfsInEcc = WebGraphBFS.calculateEccentricity(reverse, current);
            maxEcc = Math.max(maxEcc, bfsInEcc);
            numBFS++;


            timeElapsed = System.nanoTime() - start;
            millisElapsed = timeElapsed / 1_000_000;
            seccondsElapsed = ((double)millisElapsed) / 1_000;
            System.out.println("finished " + i++ + " / " + queueSize + " from queue after " + seccondsElapsed + " secconds");

        }


        return maxEcc;
    }


    public Integer run() {
        ImmutableDirectedGraphAdapter adapter = new ImmutableDirectedGraphAdapter(g, reverse);
        VertexChooser chooser = new Arbitrary_Vertex();
        int chooserInitialNodeode = chooser.getInitialNode(adapter);
        adapter = null;

        int bfsEcc = WebGraphBFS.calculateEccentricity(g, chooserInitialNodeode);

        if (h < 0) {
            h = 2 * bfsEcc / 3;
        }


        long numArcs = 0;
        int maxDeg = 0;
        for (var iter = g.nodeIterator(); iter.hasNext();) {
            int outdeg = g.outdegree(iter.nextInt());
            numArcs += outdeg;
            maxDeg = Math.max(outdeg, maxDeg);
        }

        System.out.println(numArcs);
        System.out.println(g.numNodes());

        System.out.println( 1.0 / (2 * h + 3));



        int diameter = run(Math.min((int) Math.round(Math.pow(numArcs, 1.0 / (2 * h + 3))) + 1, maxDeg));
        numBFS++;
        return diameter;
    }

    public int getNumBFS() {
        return numBFS;
    }
}