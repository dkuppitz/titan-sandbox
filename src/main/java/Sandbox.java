import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.thinkaurelius.titan.core.util.TitanCleanup;

import java.util.Iterator;

/**
 * @author Daniel Kuppitz (daniel at thinkaurelius.com)
 */
public class Sandbox {

    public static void main(final String[] args) {

        TitanGraph g = getGraph();

        g.shutdown();
        TitanCleanup.clear(g);
        g = getGraph();

        createSchema(g);
        generateSampleData(g);
        printSchemaInformation(g);
        printData(g);

        g.shutdown();
        TitanCleanup.clear(g);
    }

    private static TitanGraph getGraph() {
        return TitanFactory.build()
                .set("storage.backend", "berkeleyje")
                .set("storage.directory", "/tmp/titan-sandbox-db")
                .open();
    }

    private static void createSchema(final TitanGraph graph) {

        final TitanManagement m = graph.getManagementSystem();

        if (!m.containsPropertyKey("longValue")) {
            m.makePropertyKey("longValue").dataType(Long.class).make();
        }

        m.commit();
    }

    private static void printSchemaInformation(final TitanGraph graph) {

        final TitanManagement m = graph.getManagementSystem();

        System.out.println("\n== SCHEMA INFORMATION ==\n");
        System.out.println("Data type for 'longValue': " + (m.containsPropertyKey("longValue")
                ? (m.getPropertyKey("longValue").getDataType())
                : "unavailable"));

        m.commit();
    }

    private static void generateSampleData(final TitanGraph graph) {
        graph.addVertex().setProperty("longValue", 1L);
        graph.commit();
    }

    private static void printData(final TitanGraph graph) {

        final Iterator vertices = graph.getVertices().iterator();

        System.out.println("\n== DATA ==\n");

        if (vertices.hasNext()) {
            vertices.forEachRemaining(v -> System.out.println(((TitanVertex) v).<Long>getProperty("longValue")));
        } else {
            System.out.println("No data available");
        }
    }
}
