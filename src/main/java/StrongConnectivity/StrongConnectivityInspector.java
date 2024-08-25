package StrongConnectivity;

import org.javatuples.Pair;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.builder.GraphBuilder;
import org.jgrapht.util.CollectionUtil;

import java.util.*;

public class StrongConnectivityInspector<V, E> extends KosarajuStrongConnectivityInspector<V,E> {
    private Map<V, Graph<V, E>> vertexToComponent;



    public StrongConnectivityInspector(Graph<V,E> graph) {
        super(graph);
        vertexToComponent = null;
    }



    public Map<V, Graph<V, E>> getVertexToComponent() {
        if (vertexToComponent == null) {
            vertexToComponent = CollectionUtil.newHashMapWithExpectedSize(graph.vertexSet().size());

            final List<Graph<V,E>> sccSubGraphs = getStronglyConnectedComponents();
            for (Graph<V, E> sccSubGraph : sccSubGraphs) {
                for (V v : sccSubGraph.vertexSet()) {
                    vertexToComponent.put(v, sccSubGraph);
                }
            }
        }

        return vertexToComponent;
    }



    public Map<Pair<Graph<V,E>, Graph<V,E>>, E> getCorrespondingEdges(Comparator<E> comparator) {
        final List<Graph<V, E>> sccSubGraphs = getStronglyConnectedComponents();
        final Map<V, Graph<V, E>> vertexToComponent = getVertexToComponent();

        final Map<Pair<Graph<V,E>, Graph<V,E>>, E> correspondingEdges = new TreeMap<>(Comparator.comparingInt(o -> o.getValue0().vertexSet().size()));

        final Set<E> edgesBetweenDifferentDirectedComponents = new HashSet<>(graph.edgeSet());
        for (Graph<V,E> sccSubGraph : sccSubGraphs) {
            edgesBetweenDifferentDirectedComponents.removeAll(sccSubGraph.edgeSet());
        }

        for (E edge : edgesBetweenDifferentDirectedComponents) {
            V source = graph.getEdgeSource(edge);
            V target = graph.getEdgeTarget(edge);
            Graph<V, E> sourceComponent = vertexToComponent.get(source);
            Graph<V, E> targetComponent = vertexToComponent.get(target);

            assert sourceComponent != targetComponent; // a check that everything worked correctly


            var componentsEdgeAsPair = new Pair<>(sourceComponent, targetComponent);
            if (!correspondingEdges.containsKey(componentsEdgeAsPair)) {
                correspondingEdges.put(componentsEdgeAsPair, edge);
            } else if (comparator != null) {
                E currentBestEdge = correspondingEdges.get(componentsEdgeAsPair);

                if (comparator.compare(edge, currentBestEdge) > 0) {
                    correspondingEdges.put(componentsEdgeAsPair, edge);
                }
            }
        }

        return correspondingEdges;
    }

    @Override
    public Graph<Graph<V, E>, DefaultEdge> getCondensation() {
        final List<Graph<V,E>> sccSubGraphs = getStronglyConnectedComponents();
        final GraphBuilder<Graph<V, E> , DefaultEdge, SimpleDirectedGraph<Graph<V, E>, DefaultEdge>> builder = new GraphBuilder<>(new SimpleDirectedGraph<>(DefaultEdge.class));

        for (Graph<V, E> sccSubGraph : sccSubGraphs) {
            builder.addVertex(sccSubGraph);
        }

        final var condensationGraphEdgesAsPairs = getCorrespondingEdges(null).keySet();
        for (var edgeAsPair : condensationGraphEdgesAsPairs) {
            builder.addEdge(edgeAsPair.getValue0(), edgeAsPair.getValue1());
        }

        return builder.build();
    }
}
