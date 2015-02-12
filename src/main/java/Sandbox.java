import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.thinkaurelius.titan.core.util.TitanCleanup;
import com.tinkerpop.blueprints.Edge;

/**
 * @author Daniel Kuppitz (http://gremlin.guru)
 */
public class Sandbox {

    public static void main(final String[] args) {

        TitanGraph g = getGraph();

        createSchema(g);
        generateSampleData(g);

        g.query().has("quality").orderBy("quality", Order.DESC).edges()
                .forEach(e -> System.out.println(((Edge) e).getProperty("quality") + " :: " + e.toString()));
        g.rollback();

        // use an explicitly created transaction
        TitanTransaction tx = g.newTransaction();
        tx.query().has("quality").orderBy("quality", Order.DESC).edges()
                .forEach(e -> System.out.println(((Edge) e).getProperty("quality") + " :: " + e.toString()));
        tx.rollback();

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

        if (!m.containsPropertyKey("quality")) {
            m.makePropertyKey("quality").dataType(Integer.class).cardinality(Cardinality.SINGLE).make();
        }

        m.commit();
    }

    private static void generateSampleData(final TitanGraph graph) {
        final TitanVertex root = graph.addVertex();
        root.addEdge("link", graph.addVertex()).setProperty("quality", 42);
        root.addEdge("link", graph.addVertex()).setProperty("quality", 0);
        root.addEdge("link", graph.addVertex()).setProperty("quality", 8);
        root.addEdge("link", graph.addVertex()).setProperty("quality", 15);
        graph.commit();
    }
}
