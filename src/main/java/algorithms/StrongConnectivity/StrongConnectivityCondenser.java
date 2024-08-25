package algorithms.StrongConnectivity;

import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.builder.GraphBuilder;
import org.jgrapht.util.CollectionUtil;

import java.util.*;

public class StrongConnectivityCondenser<V, E> extends KosarajuStrongConnectivityInspector<V,E> {
    private Map<V, Integer> vertexToComponent;



    public StrongConnectivityCondenser(Graph<V,E> graph) {
        super(graph);
        vertexToComponent = null;
    }



    public Map<V, Integer> vertexToComponentNumber() {
        if (vertexToComponent == null) {
            vertexToComponent = CollectionUtil.newHashMapWithExpectedSize(graph.vertexSet().size());

            final List<Set<V>> stronglyConnectedSets = stronglyConnectedSets();
            int size = stronglyConnectedSets.size();
            for (int i = 0; i < size; i++) {
                for (V v : stronglyConnectedSets.get(i)) {
                    vertexToComponent.put(v, i);
                }
            }
        }

        return vertexToComponent;
    }



    public List<Set<V>> componentNumberToComponent() {
        return stronglyConnectedSets();
    }


    public Map<IntIntPair, E> getCorrespondingEdges(Comparator<E> comparator) {
        final Map<V, Integer> vertexToComponent = vertexToComponentNumber();

        final Map<IntIntPair, E> correspondingEdges = new TreeMap<>((o1, o2) -> {
            int temp;
            if ((temp = o1.firstInt() - o2.firstInt()) == 0) return o1.secondInt() - o2.secondInt();
            return temp;
        });

        for (E edge : graph.iterables().edges()) {
            V source = graph.getEdgeSource(edge);
            V target = graph.getEdgeTarget(edge);
            int sourceComponent = vertexToComponent.get(source);
            int targetComponent = vertexToComponent.get(target);

            if (sourceComponent == targetComponent)  {
                continue;
            }


            var componentsEdge = new IntIntImmutablePair(sourceComponent, targetComponent);
            if (!correspondingEdges.containsKey(componentsEdge)) {
                correspondingEdges.put(componentsEdge, edge);
            } else if (comparator != null) {
                E currentBestEdge = correspondingEdges.get(componentsEdge);

                if (comparator.compare(edge, currentBestEdge) > 0) {
                    correspondingEdges.put(componentsEdge, edge);
                }
            }
        }

        return correspondingEdges;

    }

    public Graph<Integer, IntIntPair> condense() {
        final List<Set<V>> stronglyConnectedSets = stronglyConnectedSets();
        final GraphBuilder<Integer , IntIntPair, SimpleDirectedGraph<Integer, IntIntPair>> builder = new GraphBuilder<>(new SimpleDirectedGraph<>(IntIntPair.class));

        final int numStronglyConnectedSets = stronglyConnectedSets.size();
        for (int i = 0; i < numStronglyConnectedSets; i++) {
            builder.addVertex(i);
        }

        final var condensationGraphEdgesAsPairs = getCorrespondingEdges(null).keySet();
        for (var edgeAsPair : condensationGraphEdgesAsPairs) {
            builder.addEdge(edgeAsPair.leftInt(), edgeAsPair.rightInt(), edgeAsPair);
        }

        return builder.build();
    }
}
