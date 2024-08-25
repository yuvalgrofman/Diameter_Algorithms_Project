package algorithms.graphParsers;

import org.jgrapht.Graph;

import org.jgrapht.graph.builder.GraphBuilder;
import algorithms.specialScanner.SpecializedScanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class GraphFromTextParser<V, E, G extends Graph<V, E>> {
    public G parse(String pathToTxtFile, G baseGraph, SpecializedScanner<V> vertexScanner) {
        GraphBuilder<V , E, G> builder = new GraphBuilder<>(baseGraph);
        
        File file = new File(pathToTxtFile);
        try(Scanner scanner = new Scanner(file)) {
            vertexScanner.setScanner(scanner);

            while (vertexScanner.hasNext()) {
                V source = vertexScanner.next();

                if (!vertexScanner.hasNext()) {
                    throw new GraphParseException("file " + pathToTxtFile + " has incorrect format");
                }

                builder.addEdge(source, vertexScanner.next());
            }
        } catch (FileNotFoundException e) {
            System.err.println("incorrect file path");
            return null;
        }

        return builder.build();
    }
}
