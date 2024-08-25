package algorithms;

import org.jgrapht.Graph;
import org.jgrapht.graph.AbstractBaseGraph;

import org.javatuples.Pair;

public class Four_Sweep<V,E> extends Diameter_Algorithm<V,E> {
    private final V s;

    public Four_Sweep(AbstractBaseGraph<V, E> g, V s) {
        super(g);
        this.s = s;
    }

    @Override
    public Integer run() {
        return four_sweep().getValue1();
    }

    public Pair<V, Integer> four_sweep() {
        Graph<V,E> g = super.getGraph();
        BFS<V, E> bfs = new BFS<>(g, s);
        V a1 = bfs.getFurthest();

        BFS<V, E> bfs2 = new BFS<>(g, a1);
        V b1 = bfs2.getFurthest();
        V r2 = bfs2.getMid(b1);
        int ecc_a1 = bfs2.getDepth(b1);

        BFS<V, E> bfs3 = new BFS<>(g, b1);
        V a2 = bfs3.getFurthest();

        BFS<V, E> bfs4 = new BFS<>(g, a2);
        V b2 = bfs4.getFurthest();
        V r1 = bfs4.getMid(b2);
        int ecc_a2 = bfs4.getDepth(b2);

        return new Pair<V, Integer>(r1, Math.max(ecc_a1, ecc_a2));

    }
}
