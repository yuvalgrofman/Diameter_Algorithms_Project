import org.jgrapht.Graph;

public class Chosen_Vertex implements VertexChooser {
    private Integer chosenVertex;

    Chosen_Vertex(Integer chosenVertex) {
        this.chosenVertex = chosenVertex;
    }

    @Override
    public <V, E> V getInitialNode(Graph<V, E> g) {
        return (V) chosenVertex;
    }
}
