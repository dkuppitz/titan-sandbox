import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.thinkaurelius.titan.core.util.TitanCleanup;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;

/**
 * @author Daniel Kuppitz (http://gremlin.guru)
 */
public class Sandbox {

    public static void main(final String[] args) {

        TitanGraph g = getGraph();

        createSchema(g);
        TitanVertex root = generateSampleData(g);

        root.query().labels("link").direction(Direction.OUT).orderBy("quality", Order.DESC).limit(2).vertices()
                .forEach(v -> System.out.println(v.<String>getProperty("name")));
        g.rollback();

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
        final EdgeLabel link = m.makeEdgeLabel("link").make();
        final PropertyKey quality = m.makePropertyKey("quality").dataType(Integer.class).cardinality(Cardinality.SINGLE).make();

        m.makePropertyKey("name").dataType(String.class).make();
        m.buildEdgeIndex(link, "linkByQuality", Direction.OUT, Order.DESC, quality);
        m.commit();
    }

    private static TitanVertex generateSampleData(final TitanGraph graph) {
        final TitanVertex root = graph.addVertex();
        final TitanVertex v1, v2, v3, v4;
        root.addEdge("link", v1 = graph.addVertex()).setProperty("quality", 42);
        root.addEdge("link", v2 = graph.addVertex()).setProperty("quality", 0);
        root.addEdge("link", v3 = graph.addVertex()).setProperty("quality", 8);
        root.addEdge("link", v4 = graph.addVertex()).setProperty("quality", 15);
        v1.setProperty("name", "inV for quality 42");
        v2.setProperty("name", "inV for quality 0");
        v3.setProperty("name", "inV for quality 8");
        v4.setProperty("name", "inV for quality 15");
        graph.commit();
        return root;
    }
}
