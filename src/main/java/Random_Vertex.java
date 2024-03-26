import org.jgrapht.Graph;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class Random_Vertex implements VertexChooser {
    @Override
    public <V, E> V getInitialNode(Graph<V, E> g) {
        Set<V> vertices = g.vertexSet();
        int size = vertices.size();
        int index = new Random().nextInt(size);
        Iterator<V> iter = vertices.iterator();
        for (int i = 0; i < index; i++) {
            iter.next();
        }
        return iter.next();
    }
}

