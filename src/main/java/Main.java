import algorithms.*;
import algorithmsForWebGraphs.*;
import com.martiansoftware.jsap.JSAPException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntIntSortedPair;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.ImmutableSubgraph;
import it.unimi.dsi.webgraph.Transform;
import it.unimi.dsi.webgraph.algo.SumSweepDirectedDiameterRadius;

import org.javatuples.Pair;
import org.jgrapht.webgraph.ImmutableDirectedGraphAdapter;
import org.jgrapht.webgraph.ImmutableUndirectedGraphAdapter;

class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException, JSAPException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        final String datasetPath = "/home/jonathan/IdeaProjects/Diameter_Algorithms_Project___/dataset/";
        final String[] basenames = {
                 "cnr-2000",
                 "in-2004",
                 "eu-2005",
                 "uk-2007-05@100000",
                 "uk-2007-05@1000000",
                  "uk-2014-host",
////                //  "eu-2015-host",
////                //  "gsh-2015-host",
                  "uk-2014-tpd",
////////                  "eu-2015-tpd",
////////                      "gsh-2015-tpd",
                  "uk-2002",
                  "indochina-2004",
                "it-2004",
                "arabic-2005",
                "sk-2005",
                  "uk-2005",
                "enwiki-2013",
                 "enwiki-2015",
                 "enwiki-2016",
                 "enwiki-2017",
                 "enwiki-2018",
                 "enwiki-2019",
                 "enwiki-2020",
                 "enwiki-2021",
////                // "enwiki-2022",
//                // "itwiki-2013",
                 "eswiki-2013",
                // // "frwiki-2013",
                // // "dewiki-2013",
                "enron",
////                // "amazon-2008",
//////                  "ljournal-2008",
////                //  "hollywood-2009",
////                // // "hollywood-2011",
////                // "imdb-2021",
              "dblp-2010",

              "dblp-2011",
////                // "twitter-2010",
               "wordassociation-2011",
////
////
                 "amazon-2008",
              "hollywood-2009",
                "imdb-2021"


//                "uk-2006-05",
//                "uk-2006-06",
//                "uk-2006-07",
//                "uk-2006-08",
//                "uk-2006-09",
//                "uk-2006-10",
//                "uk-2006-11",
//                "uk-2006-12",
//                "uk-2007-01",
//                "uk-2007-02",
//                "uk-2007-03",
//                "uk-2007-04",
//                "uk-2007-05",
//                "webbase-2001",

                // "uk-2014",
                // "eu-2015",
                // "gsh-2015",
                // "clueweb12",
                // "hu-tel-2006", only suxdir
                // "orkut-2007", only suxdir
        };

//        Path file = Path.of("/home/jonathan/IdeaProjects/Diameter_Algorithms_Project___/results/IFubFourSweep");
//        Files.writeString(file, "basename" + "\t\t\t" + "result" + "\t\t\t" + "numbfs of ifub (not including four sweep)" + "\n", StandardCharsets.UTF_8);

        VertexChooser chooser = new Highest_Degree(Highest_Degree.Option.TOTAL_DEGREE);
        for (String basename : basenames) {
            String path = datasetPath + basename + "/" + basename;
            ImmutableGraph largewcc = ImmutableSubgraph.load(path + "-largewcc");
            ImmutableGraph graph = BVGraph.load(path);

            long numEdgesInLargeWcc = 0;
            for (var iter = largewcc.nodeIterator(); iter.hasNext();) {
                numEdgesInLargeWcc += largewcc.outdegree(iter.nextInt());
            }

            System.out.println("\\text{" + basename + "} & " + graph.numNodes() + " & " + graph.numArcs()
                    + " & " + largewcc.numNodes() + " & " + numEdgesInLargeWcc + " \\");
            System.out.println("\\hline");
        }
    }
}