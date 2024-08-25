import org.jgrapht.Graph;

import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.Set;

public class BFS<V,E> {
    private final Graph<V,E> graph;
    private final V start;
    private final V furthest;
    private final BreadthFirstIterator<V,E> iterator;


    BFS(Graph<V,E> graph, V start) {
        this.graph = graph;
        this.start = start;

        iterator = new BreadthFirstIterator<>(graph, start);

        V currentFurthest = start;
        while (iterator.hasNext()) {
            currentFurthest = iterator.next();
        }
        furthest = currentFurthest;
    }

    public V getFurthest() {
        return furthest;
    }

    public int getDepth(V t) {
        return iterator.getDepth(t);
    }

    public int getEcc() {
        return iterator.getDepth(furthest);
    }

    private V getNthParent(V v, int n) {
        V parent = v;
        for (int i = 0; i < n; i++) {
            parent = iterator.getParent(parent);
        }
        return parent;
    }

    public V getMid(V t) {
        return getNthParent(t, getEcc() / 2);
    }
}