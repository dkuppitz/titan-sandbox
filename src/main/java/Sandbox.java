import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.thinkaurelius.titan.core.util.TitanCleanup;
import com.tinkerpop.blueprints.Vertex;

import java.util.UUID;

/**
 * @author Daniel Kuppitz (http://gremlin.guru)
 */
public class Sandbox {

    public static void main(final String[] args) {

        TitanGraph g = getGraph();

        createSchema(g);

        final long t1 = System.currentTimeMillis(), t2;
        final String id = UUID.randomUUID().toString();
        final Vertex customer = g.addVertex();
        customer.setProperty("something", id);
        g.commit();

        t2 = System.currentTimeMillis();

        long t3, t4;
        final Iterable<Vertex> iter = g.query().has("something", id).vertices();
        for (final Vertex v : iter) {
            t3 = System.currentTimeMillis();
            if (v.getProperty("something").equals(id)) {
                t4 = System.currentTimeMillis();
                System.err.println("Insert took [ms]: " + (t2 - t1));
                System.err.println("Vertex retrieval [ms]: " + (t3 - t2));
                System.err.println("Property retrieval [ms]: " + (t4 - t3));
                break;
            }
        }

        g.rollback();

        g.shutdown();
        TitanCleanup.clear(g);
    }

    private static TitanGraph getGraph() {
        return TitanFactory.build()
                .set("storage.backend", "cassandra")
                        //.set("storage.directory", "/tmp/titan-sandbox-db")
                .set("storage.hostname", "localhost")
                .open();
    }

    private static void createSchema(final TitanGraph graph) {
        final TitanManagement m = graph.getManagementSystem();
        final PropertyKey something = m.makePropertyKey("something").dataType(String.class).make();
        m.buildIndex("bySomething", Vertex.class).addKey(something).buildCompositeIndex();
        m.commit();
    }
}
