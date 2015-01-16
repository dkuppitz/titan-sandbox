import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.thinkaurelius.titan.core.util.TitanCleanup;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.structure.Vertex;

import java.util.Map;

/**
 * @author Daniel Kuppitz (daniel at thinkaurelius.com)
 */
public class Sandbox {

    public static void main(final String[] args) {

        TitanGraph g = getGraph();

        createSchema(g);
        generateSampleData(g);
        printSchemaInformation(g);
        printData(g);

        g.close();
        TitanCleanup.clear(g);

        g = getGraph();
        printSchemaInformation(g);
        printData(g);

        g.close();
    }

    private static TitanGraph getGraph() {
        return TitanFactory.build()
                .set("storage.backend", "berkeleyje")
                .set("storage.directory", "/tmp/titan-sandbox-db")
                .open();
    }

    private static void createSchema(final TitanGraph graph) {

        final TitanManagement m = graph.openManagement();

        if (!m.containsVertexLabel("person")) {
            m.makeVertexLabel("person").make();
        }

        if (!m.containsPropertyKey("name")) {
            m.makePropertyKey("name").dataType(String.class).make();
        }

        if (!m.containsPropertyKey("keywords")) {
            m.makePropertyKey("keywords").dataType(String.class).make();
        }

        m.commit();
    }

    private static void printSchemaInformation(final TitanGraph graph) {

        final TitanManagement m = graph.openManagement();

        System.out.println("\n== SCHEMA INFORMATION ==\n");
        System.out.println("Vertex label 'person':   " + (m.containsVertexLabel("person") ? "available" : "unavailable"));
        System.out.println("Property key 'name':     " + (m.containsPropertyKey("name") ? "available" : "unavailable"));
        System.out.println("Property key 'keywords': " + (m.containsPropertyKey("keywords") ? "available" : "unavailable"));

        m.commit();
    }

    private static void generateSampleData(final TitanGraph graph) {
        graph.addVertex("name", "Mark", "keywords", "shocked");
        graph.tx().commit();
    }

    private static void printData(final TitanGraph graph) {

        final GraphTraversal<Vertex, Map<String, Object>> traversal = graph.V().valueMap();

        System.out.println("\n== DATA ==\n");

        if (traversal.hasNext()) {
            graph.V().valueMap().forEachRemaining(System.out::println);
        } else {
            System.out.println("No data available");
        }
    }
}
