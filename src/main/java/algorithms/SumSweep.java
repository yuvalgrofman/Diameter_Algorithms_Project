package algorithms;

import org.jgrapht.Graph;
import org.javatuples.Pair;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.EdgeReversedGraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SumSweep<V,E> extends Diameter_Algorithm<V,E> {
    private V s;
    private int k;
    private Set<V> vPrime;

    @Override
    public Integer run() {
        return sumSweep().getValue0();
    }


    public SumSweep(AbstractBaseGraph<V, E> g, V s, int k, Set<V> vPrime) {
        super(g);
        this.s = s;
        this.k = k;
        this.vPrime = vPrime;
    }


    public Pair<Integer, Integer> sumSweep() {
        Graph<V, E> reverseGraph = new EdgeReversedGraph<>(getGraph());

        int n = getGraph().vertexSet().size();

        HashMap<V, Integer> sF = HashMap.newHashMap(n);
        HashMap<V, Integer> sB = HashMap.newHashMap(n);

        Set<V> f = new HashSet<>();
        Set<V> b = new HashSet<>();
        f.add(s);

        BFS<V,E> bfs = new BFS<>(getGraph(), s);

        int dL = bfs.getEcc();

        for (V x : getGraph().vertexSet()) {
            int value;
            try {
                value = bfs.getDepth(x);
            } catch (NullPointerException exception) {
                value = 0;
            }
            sB.put(x, value);
            sF.put(x, 0);
        }


        for (int i = 2; i < k; i++) {
            if (i % 2 == 1) {
                int max = -1;
                int current;
                for (V x : getGraph().vertexSet()) {
                    if (!f.contains(x)) { // s = argmax..
                        current = sF.get(x);
                        if (current > max) {
                            max = current;
                            s = x;
                        }
                    }
                }
                f.add(s);
                bfs = new BFS<>(getGraph(), s);
                dL = Math.max(dL, bfs.getEcc());


                for (V x : getGraph().vertexSet()) {
                    int dist = 0;

                    try {
                        dist = bfs.getDepth(x);
                    } catch (NullPointerException exception) {
                        dist = 0;
                    } finally {
                        int updatedSbVal = dist + sB.get(x);
                        sB.replace(x, updatedSbVal);
                    }
                }
            }

            else {
                int max = -1;
                int current;
                for (V x : getGraph().vertexSet()) {
                    if (!b.contains(x)) { // s = argmax..
                        current = sB.get(x);
                        if (current > max) {
                            max = current;
                            s = x;
                        }
                    }
                }
                b.add(s);

                bfs = new BFS<>(reverseGraph, s);
                dL = Math.max(dL, bfs.getEcc());


                for (V x : getGraph().vertexSet()) {
                    int dist = 0;

                    try {
                        dist = bfs.getDepth(x);
                    } catch (NullPointerException exception) {
                        dist = 0;
                    } finally {
                        int updatedSfVal = dist + sF.get(x);
                        sF.replace(x, updatedSfVal);
                    }
                }
            }
        }

        if (vPrime == null ||  vPrime.isEmpty()) {
            return new Pair<>(dL, null);
        }

        int min = 0;
        boolean isFirst = true;
        int current;
        for (V x : vPrime) {
            if (isFirst) {
                min = sF.get(x);
                isFirst = false;
            }
            else {
                current = sF.get(x);
                if (current < min) {
                    min = current;
                    s = x;
                }
            }
        }

        bfs = new BFS<>(getGraph(), s);
        int rU = bfs.getEcc();

        return new Pair<>(dL, rU);
    }
}
