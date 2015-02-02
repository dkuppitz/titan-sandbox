import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;
import com.thinkaurelius.titan.core.util.TitanCleanup;
import com.thinkaurelius.titan.example.GraphOfTheGodsFactory;
import com.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Kuppitz (daniel at thinkaurelius.com)
 */
public class Sandbox {

    public static void main(final String[] args) {

        TitanGraph g = getGraph();
        g.close();
        TitanCleanup.clear(g);

        g = getGraph();
        generateSampleData(g);
        countGremlin(g);
        countMultiQueryWrong(g);
        countMultiQuery(g);

        g.close();
        TitanCleanup.clear(g);
    }

    private static TitanGraph getGraph() {
        return TitanFactory.build()
                .set("storage.backend", "cassandrathrift")
                .set("storage.hostname", "localhost")
                .set("index.search.backend", "elasticsearch")
                .set("index.search.hostname", "localhost")
                .set("index.search.elasticsearch.client-only", true)
                .open();
    }

    private static void generateSampleData(final TitanGraph graph) {
        GraphOfTheGodsFactory.load(graph);
    }

    private static void countGremlin(final TitanGraph graph) {

        System.out.println("\n== Gremlin ==\n");

        System.out.print("g.V().out('brother').out('lives').count() ==> ");
        graph.V().out("brother").out("lives").count().forEachRemaining(System.out::println);

        System.out.print("g.V().out('brother').out('lives').in('lives').count() ==> ");
        graph.V().out("brother").out("lives").in("lives").count().forEachRemaining(System.out::println);
    }


    private static void countMultiQueryWrong(final TitanGraph graph) {

        System.out.println("\n== MultiQuery (the wrong way) ==\n");

        final List<Vertex> gods = (List<Vertex>) (Object) graph.V().out("brother").toList();
        final List<TitanVertex> level1 = new ArrayList<>();
        final List<TitanVertex> level2 = new ArrayList<>();

        // level 1
        graph.multiQuery().addAllVertices(gods).direction(Direction.OUT).labels("lives").vertices().forEach((outV, inVs) -> {
            for (final TitanVertex v : (Iterable<TitanVertex>) inVs) {
                level1.add(v);
            }
        });

        // level 2
        graph.multiQuery().addAllVertices(level1).direction(Direction.IN).labels("lives").vertices().forEach((inV, outVs) -> {
            for (final TitanVertex v : (Iterable<TitanVertex>) outVs) {
                level2.add(v);
            }
        });

        System.out.print("g.V().out('brother').out('lives').count() ==> ");
        System.out.println(level1.size());

        System.out.print("g.V().out('brother').out('lives').in('lives').count() ==> ");
        System.out.println(level2.size());
    }

    private static void countMultiQuery(final TitanGraph graph) {

        System.out.println("\n== MultiQuery (the right way) ==\n");

        final Map<Vertex, Long> gods = (Map<Vertex, Long>) graph.V().out("brother").groupCount().cap().next();
        final Map<TitanVertex, Long> level1 = new HashMap<>();
        final Map<TitanVertex, Long> level2 = new HashMap<>();

        // level 1
        graph.multiQuery().addAllVertices(gods.keySet()).direction(Direction.OUT).labels("lives").vertices().forEach((outV, inVs) -> {
            final Long x = gods.get(outV);
            for (final TitanVertex v : (Iterable<TitanVertex>) inVs) {
                level1.compute(v, (tv, c) -> (c != null ? c : 0L) + x);
            }
        });

        // level 2
        graph.multiQuery().addAllVertices(level1.keySet()).direction(Direction.IN).labels("lives").vertices().forEach((inV, outVs) -> {
            final Long x = level1.get(inV);
            for (final TitanVertex v : (Iterable<TitanVertex>) outVs) {
                level2.compute(v, (tv, c) -> (c != null ? c : 0L) + x);
            }
        });

        System.out.print("g.V().out('brother').out('lives').count() ==> ");
        System.out.println(level1.values().stream().mapToLong(x -> x).sum());

        System.out.print("g.V().out('brother').out('lives').in('lives').count() ==> ");
        System.out.println(level2.values().stream().mapToLong(x -> x).sum());
    }
}
