import org.javatuples.Pair;
import org.jgrapht.Graph;

public class IFub<V,E> {
    private final Graph<V,E> g;
    private final VertexChooser chooser;

    public IFub(Graph<V, E> g, VertexChooser chooser) {
        this.g = g;
        this.chooser = chooser;
    }

    public Integer run() {
        V u = chooser.getInitialNode(g);
        return run(u, 0 , 0);
    }

    public Integer run(V u, int l, int k) {
        BFS<V, E> bfs = new BFS<>(g, u);
        int ecc_u = bfs.getEcc();
        int i = ecc_u;
        int lb = Math.max(l, ecc_u);
        int ub = 2 * ecc_u;

        while (ub - lb > k) {
            int bi_u = (lb + ub) / 2; // TODO: fix with moss
            if (Math.max(lb, bi_u) > 2 * (i - 1)) {
                return Math.max(lb, bi_u);
            }
            lb = Math.max(lb, bi_u);
            ub = 2 * (i - 1);
            i = i - 1;
        }

        return lb;
    }
}
