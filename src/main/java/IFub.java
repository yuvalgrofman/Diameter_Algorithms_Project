import org.jgrapht.Graph;

import java.util.Set;

public class IFub<V,E> {
    private final Graph<V,E> g;
    private final VertexChooser chooser;
    private int numBFS;
    private int numLayeredBFS;

    public IFub(Graph<V, E> g, VertexChooser chooser) {
        this.g = g;
        this.chooser = chooser;
    }

    public Integer run() {
        numBFS = 0;
        numLayeredBFS = 0;
        V u = chooser.getInitialNode(g);
        return run(u, 0 , 0);
    }

    public Integer run(V u, int l, int k) {
        BFS<V, E> bfs = new BFS<>(g, u);
        numBFS++;
        int ecc_u = bfs.getEcc();
        int i = ecc_u;
        int lb = Math.max(l, ecc_u);
        int ub = 2 * ecc_u;

        while (ub - lb > k) {
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
    public int getMaxEccOfLayer(V v, int i) {
        Layered_BFS<V, E> bfs = new Layered_BFS<>(g, v, i);
        numLayeredBFS++;
        Set<V> layer = bfs.getLayer();

        int maxEcc = 0;
        for (V vertex : layer) {
            BFS<V, E> bfs2 = new BFS<>(g, vertex);
            numBFS++;
            int ecc = bfs2.getEcc();
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